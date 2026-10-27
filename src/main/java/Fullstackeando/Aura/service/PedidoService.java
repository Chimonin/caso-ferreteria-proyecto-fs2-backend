//reglas de negocio de los pedidos
package Fullstackeando.Aura.Ferreteria_Los_Maestros.service;

import Fullstackeando.Aura.Ferreteria_Los_Maestros.dto.CrearPedidoRequest;
import Fullstackeando.Aura.Ferreteria_Los_Maestros.dto.ItemCarritoRequest;
import Fullstackeando.Aura.Ferreteria_Los_Maestros.exception.ReglaNegocioException;
import Fullstackeando.Aura.Ferreteria_Los_Maestros.model.Cliente;
import Fullstackeando.Aura.Ferreteria_Los_Maestros.model.DetallePedido;
import Fullstackeando.Aura.Ferreteria_Los_Maestros.model.Pedido;
import Fullstackeando.Aura.Ferreteria_Los_Maestros.model.Producto;
import Fullstackeando.Aura.Ferreteria_Los_Maestros.repository.ClienteRepository;
import Fullstackeando.Aura.Ferreteria_Los_Maestros.repository.PedidoRepository;
import Fullstackeando.Aura.Ferreteria_Los_Maestros.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PedidoService {

    private static final int COSTO_DESPACHO = 3990;

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProductoRepository productoRepository,
                         ClienteRepository clienteRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
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
    //@Transactional: si algo falla a mitad de camino, se deshace todo
    //(reemplaza el "las validaciones se hacen antes de guardar" del frontend)
    @Transactional
    public Pedido crearPedido(CrearPedidoRequest request) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado."));

        String medioPago = request.getMedioPago();
        if (medioPago.equals("cuenta_corriente") && !cliente.isCuentaCorrienteHabilitada()) {
            throw new ReglaNegocioException("La cuenta corriente no está habilitada.");
        }

        Pedido nuevoPedido = new Pedido();
        int subtotal = 0;

        for (ItemCarritoRequest itemCarrito : request.getItems()) {
            //un producto eliminado (activo = false) cuenta como que ya no existe
            Producto producto = productoRepository.findById(itemCarrito.getCodigo())
                    .filter(Producto::isActivo)
                    .orElseThrow(() -> new ReglaNegocioException("Uno de los productos ya no existe."));

            if (itemCarrito.getCantidad() > producto.getStock()) {
                throw new ReglaNegocioException("No hay stock suficiente de " + producto.getNombre() + ".");
            }

            DetallePedido item = new DetallePedido();
            item.setPedido(nuevoPedido);
            item.setProducto(producto);
            item.setCantidad(itemCarrito.getCantidad());
            item.setPrecioUnitario(producto.getPrecio());
            nuevoPedido.getItems().add(item);

            subtotal += item.getPrecioUnitario() * item.getCantidad();

            //descuenta el stock
            producto.setStock(producto.getStock() - itemCarrito.getCantidad());
            productoRepository.save(producto);
        }

        String modalidad = request.getEntrega().getModalidad();
        int costoEnvio = modalidad.equals("despacho") ? COSTO_DESPACHO : 0;
        int total = subtotal + costoEnvio;

        nuevoPedido.setClienteId(request.getClienteId());
        nuevoPedido.setFecha(LocalDate.now());
        nuevoPedido.setEstado("Pendiente");
        nuevoPedido.setMedioPago(medioPago);
        nuevoPedido.setModalidadEntrega(modalidad);
        //si es retiro, comuna y direccion quedan vacias igual que en Envios.jsx
        if (modalidad.equals("despacho")) {
            nuevoPedido.setComunaEntrega(request.getEntrega().getComuna().trim());
            nuevoPedido.setDireccionEntrega(request.getEntrega().getDireccion().trim());
        }
        nuevoPedido.setCostoEnvio(costoEnvio);
        nuevoPedido.setTotal(total);

        //la compra a credito se suma a la deuda
        if (medioPago.equals("cuenta_corriente")) {
            cliente.setSaldoAdeudado(cliente.getSaldoAdeudado() + total);
            clienteRepository.save(cliente);
        }

        //cascade all: tambien guarda los items en detalle_pedido
        return pedidoRepository.save(nuevoPedido);
    }
}
