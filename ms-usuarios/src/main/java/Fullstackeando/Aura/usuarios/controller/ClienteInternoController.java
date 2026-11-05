package Fullstackeando.Aura.usuarios.controller;

import Fullstackeando.Aura.usuarios.dto.*;
import Fullstackeando.Aura.usuarios.service.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/internal/clientes")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "internalKey")
public class ClienteInternoController {
    private final ClienteService consultas;
    private final ClienteGestionService gestion;
    public ClienteInternoController(ClienteService consultas, ClienteGestionService gestion) {
        this.consultas = consultas; this.gestion = gestion;
    }

    @GetMapping("/{id}")
    public ClienteResponse obtenerCliente(@PathVariable Long id) { return consultas.obtenerCliente(id); }

    @PatchMapping("/{id}/sumar-deuda")
    public ResponseEntity<Void> sumarDeuda(@PathVariable Long id, @Valid @RequestBody MontoRequest datos) {
        gestion.cambiarDeuda(id, datos.monto(), true);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restar-deuda")
    public ResponseEntity<Void> restarDeuda(@PathVariable Long id, @Valid @RequestBody MontoRequest datos) {
        gestion.cambiarDeuda(id, datos.monto(), false);
        return ResponseEntity.noContent().build();
    }
}
