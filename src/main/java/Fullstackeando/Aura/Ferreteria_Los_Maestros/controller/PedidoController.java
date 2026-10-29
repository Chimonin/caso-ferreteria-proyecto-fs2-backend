package Fullstackeando.Aura.Ferreteria_Los_Maestros.controller;

import Fullstackeando.Aura.Ferreteria_Los_Maestros.dto.CambiarEstadoRequest;
import Fullstackeando.Aura.Ferreteria_Los_Maestros.dto.CrearPedidoRequest;
import Fullstackeando.Aura.Ferreteria_Los_Maestros.model.DetallePedido;
import Fullstackeando.Aura.Ferreteria_Los_Maestros.model.Pedido;
import Fullstackeando.Aura.Ferreteria_Los_Maestros.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    //GET /api/pedidos -> listarPedidos() vendedor y admin en gestion de pedidos
    @GetMapping
    public List<Map<String, Object>> listarPedidos() {
        return pedidoService.listarPedidos().stream()
                .map(this::aRespuesta)
                .toList();
    }

    //GET /api/pedidos/cliente/3 -> listarPedidosCliente(3) mis pedidos
    @GetMapping("/cliente/{clienteId}")
    public List<Map<String, Object>> listarPedidosCliente(@PathVariable Long clienteId) {
        return pedidoService.listarPedidosCliente(clienteId).stream()
                .map(this::aRespuesta)
                .toList();
    }

    //POST /api/pedidos -> crearPedido(...) pago
    @PostMapping
    public ResponseEntity<Map<String, Object>> crearPedido(@Valid @RequestBody CrearPedidoRequest request) {
        Pedido pedido = pedidoService.crearPedido(request);

        URI location = URI.create("/api/pedidos/" + pedido.getId());
        return ResponseEntity.created(location).body(aRespuesta(pedido));
    }

    //PATCH /api/pedidos/1/estado -> cambiarEstado(1, nuevoEstado)
    @PatchMapping("/{id}/estado")
    public Map<String, Object> cambiarEstado(@PathVariable Long id,
                                             @Valid @RequestBody CambiarEstadoRequest request) {
        return aRespuesta(pedidoService.cambiarEstado(id, request.getEstado()));
    }

    //dto de salida del backend
    private Map<String, Object> aRespuesta(Pedido p) {
        Map<String, Object> entrega = new LinkedHashMap<>();
        entrega.put("modalidad", p.getModalidadEntrega());
        entrega.put("comuna", p.getComunaEntrega());
        entrega.put("direccion", p.getDireccionEntrega());

        List<Map<String, Object>> items = p.getItems().stream()
                .map(this::aRespuestaItem)
                .toList();

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", p.getId());
        r.put("clienteId", p.getClienteId());
        r.put("fecha", p.getFecha().toString());
        r.put("estado", p.getEstado());
        r.put("medioPago", p.getMedioPago());
        r.put("entrega", entrega);
        r.put("costoEnvio", p.getCostoEnvio());
        r.put("items", items);
        r.put("total", p.getTotal());
        return r;
    }

    //como cada pedido tiene una lista de items
    //tambien necesitamos un dto de salida para los items como ayuda para pedido
    private Map<String, Object> aRespuestaItem(DetallePedido d) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("codigo", d.getProducto().getCodigo());
        r.put("nombre", d.getProducto().getNombre());
        r.put("cantidad", d.getCantidad());
        r.put("precioUnitario", d.getPrecioUnitario());
        return r;
    }
}
