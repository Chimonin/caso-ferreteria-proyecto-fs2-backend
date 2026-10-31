
//reglas de negocio de los pedidos
package Fullstackeando.Aura.pedidos.service;

import Fullstackeando.Aura.pedidos.client.ClienteClient;
import Fullstackeando.Aura.pedidos.client.ClienteRemoto;
import Fullstackeando.Aura.pedidos.client.ProductoClient;
import Fullstackeando.Aura.pedidos.client.ProductoDescontado;
import Fullstackeando.Aura.pedidos.dto.CrearPedidoRequest;
import Fullstackeando.Aura.pedidos.exception.ReglaNegocioException;
import Fullstackeando.Aura.pedidos.model.DetallePedido;
import Fullstackeando.Aura.pedidos.model.Pedido;
import Fullstackeando.Aura.pedidos.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PedidoService {

    private static final int COSTO_DESPACHO = 3990;

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepository pedidoRepository;
    //en microservicios ya no usamos ProductoRepository ni ClienteRepository:
    //esos datos viven en otros servicios y se piden por HTTP con estos clients
    private final ProductoClient productoClient;
    private final ClienteClient clienteClient;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProductoClient productoClient,
                         ClienteClient clienteClient) {
        this.pedidoRepository = pedidoRepository;
        this.productoClient = productoClient;
        this.clienteClient = clienteClient;
    }

    //devuelve todos los pedidos
    //se usa en geston de pedidos admin y vendedor y en reporte
    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    public List<Pedido> listarPedidosCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }

    //cambia el estado de un pedido

    //transactional significa que hace una sola operacion en la base de datos
    //o guarda todo o nada
    @Transactional
    public Pedido cambiarEstado(Long id, String nuevoEstado) {
        if (!Pedido.ESTADOS_PEDIDO.contains(nuevoEstado)) {
            throw new ReglaNegocioException("Estado de pedido no válido");
        }

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado"));

        if (pedido.getEstado().equals("Entregado")) {
            throw new ReglaNegocioException("Un pedido entregado ya no se puede cambiar");
        }

        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    //crea el pedido usando el carrito y descuenta el stock
    //@Transactional solo cubre la base de pedidos: el stock y la deuda estan en otras bases
    //por eso, si algo falla despues de descontar, hay que deshacerlo a mano (compensacion)
    @Transactional
    public Pedido crearPedido(CrearPedidoRequest request) {
        //1. pregunta a ms-usuarios por el cliente (404 si no existe)
        ClienteRemoto cliente = clienteClient.obtenerCliente(request.getClienteId());

        String medioPago = request.getMedioPago();
        if (medioPago.equals("cuenta_corriente") && !cliente.isCuentaCorrienteHabilitada()) {
            throw new ReglaNegocioException("La cuenta corriente no está habilitada.");
        }

        //2. pide a ms-productos que descuente el stock de todo el carrito de una vez
        //si un producto no existe o no tiene stock, no descuenta nada y lanza el error
        //("Uno de los productos ya no existe." o "No hay stock suficiente de ...")
        List<ProductoDescontado> productos = productoClient.descontarStock(request.getItems());

        //desde aqui el stock ya esta descontado en ms-productos
        //si algo falla, el catch lo devuelve
        boolean deudaSumada = false;
        int total = 0;
        try {
            Pedido nuevoPedido = new Pedido();
            int subtotal = 0;

            for (ProductoDescontado producto : productos) {
                DetallePedido item = new DetallePedido();
                item.setPedido(nuevoPedido);
                item.setProductoCodigo(producto.getCodigo());
                item.setNombre(producto.getNombre());
                item.setCantidad(producto.getCantidad());
                //el precio lo manda ms-productos, no el frontend
                item.setPrecioUnitario(producto.getPrecio());
                //obtiene la lista con getitems y le agrega un item con add
                nuevoPedido.getItems().add(item);

                subtotal += item.getPrecioUnitario() * item.getCantidad();
            }

            String modalidad = request.getEntrega().getModalidad();
            int costoEnvio = modalidad.equals("despacho") ? COSTO_DESPACHO : 0;
            total = subtotal + costoEnvio;

            nuevoPedido.setClienteId(request.getClienteId());
            nuevoPedido.setFecha(LocalDate.now());
            nuevoPedido.setEstado("Pendiente");
            nuevoPedido.setMedioPago(medioPago);
            nuevoPedido.setModalidadEntrega(modalidad);

            if (modalidad.equals("despacho")) {
                nuevoPedido.setComunaEntrega(request.getEntrega().getComuna().trim());
                nuevoPedido.setDireccionEntrega(request.getEntrega().getDireccion().trim());
            }
            nuevoPedido.setCostoEnvio(costoEnvio);
            nuevoPedido.setTotal(total);

            //3. la compra a credito se suma a la deuda en ms-usuarios
            if (medioPago.equals("cuenta_corriente")) {
                clienteClient.sumarDeuda(request.getClienteId(), total);
                deudaSumada = true;
            }

            //4. guarda el pedido en la base de pedidos
            //cascade all: tambien guarda los items en detalle_pedido
            //saveAndFlush hace el INSERT ahora, para que un error caiga dentro de este try
            return pedidoRepository.saveAndFlush(nuevoPedido);

        } catch (RuntimeException e) {
            //compensacion: deshace en los otros servicios lo que ya se habia hecho
            if (deudaSumada) {
                final int totalDeshacer = total;
                compensar(() -> clienteClient.restarDeuda(request.getClienteId(), totalDeshacer));
            }
            compensar(() -> productoClient.reponerStock(request.getItems()));
            throw e;
        }
    }

    //ejecuta una compensacion sin tapar el error original
    //si la compensacion tambien falla, queda anotado en la consola para revisarlo a mano
    private void compensar(Runnable accion) {
        try {
            accion.run();
        } catch (RuntimeException e) {
            log.error("No se pudo deshacer un cambio en otro servicio: {}", e.getMessage());
        }
    }
}
