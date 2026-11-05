package Fullstackeando.Aura.usuarios.controller;

import Fullstackeando.Aura.usuarios.dto.ClienteResponse;
import Fullstackeando.Aura.usuarios.service.ClienteService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import Fullstackeando.Aura.usuarios.dto.ClienteRequest;
import Fullstackeando.Aura.usuarios.dto.CrearClienteRequest;
import Fullstackeando.Aura.usuarios.service.ClienteGestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import java.net.URI;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteGestionService gestion;

    public ClienteController(ClienteService clienteService, ClienteGestionService gestion) {
        this.clienteService = clienteService;
        this.gestion = gestion;
    }

    @GetMapping
    public List<ClienteResponse> listarClientes() {
        return clienteService.listarClientes();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and #id == authentication.principal.id)")
    public ClienteResponse obtenerCliente(@PathVariable Long id) {
        return clienteService.obtenerCliente(id);
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> crearCliente(@Valid @RequestBody CrearClienteRequest datos) {
        var cliente = gestion.crearCliente(datos);
        return ResponseEntity.created(URI.create("/api/clientes/" + cliente.getId())).body(cliente);
    }

    @PutMapping("/{id}")
    public ClienteResponse actualizarCliente(@PathVariable Long id, @Valid @RequestBody ClienteRequest datos) {
        return gestion.actualizarCliente(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        gestion.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }
}
