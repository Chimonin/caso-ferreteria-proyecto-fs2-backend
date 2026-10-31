package Fullstackeando.Aura.pagos.controller;

import Fullstackeando.Aura.pagos.dto.PagoRequest;
import Fullstackeando.Aura.pagos.model.Pago;
import Fullstackeando.Aura.pagos.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    //GET /api/pagos -> listarPagos() admincredito
    @GetMapping
    public List<Map<String, Object>> listarPagos() {
        return pagoService.listarPagos().stream()
                .map(this::aRespuesta)
                .toList();
    }

    //GET /api/pagos/cliente/4 -> listarPagosCliente(4)
    @GetMapping("/cliente/{clienteId}")
    public List<Map<String, Object>> listarPagosCliente(@PathVariable Long clienteId) {
        return pagoService.listarPagosCliente(clienteId).stream()
                .map(this::aRespuesta)
                .toList();
    }

    //POST /api/pagos -> registrarPago(clienteId, monto) vista mi credito
    @PostMapping
    public ResponseEntity<Map<String, Object>> registrarPago(@Valid @RequestBody PagoRequest request) {
        Pago pago = pagoService.registrarPago(request.getClienteId(), request.getMonto());

        URI location = URI.create("/api/pagos/" + pago.getId());
        return ResponseEntity.created(location).body(aRespuesta(pago));
    }

    //dto de salida del backend
    private Map<String, Object> aRespuesta(Pago p) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", p.getId());
        r.put("clienteId", p.getClienteId());
        r.put("fecha", p.getFecha().toString());
        r.put("monto", p.getMonto());
        return r;
    }
}
