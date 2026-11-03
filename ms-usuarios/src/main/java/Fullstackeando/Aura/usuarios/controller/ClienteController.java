package Fullstackeando.Aura.usuarios.controller;

import Fullstackeando.Aura.usuarios.dto.ClienteResponse;
import Fullstackeando.Aura.usuarios.service.ClienteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<ClienteResponse> listarClientes() {
        return clienteService.listarClientes();
    }

    @GetMapping("/{id}")
    public ClienteResponse obtenerCliente(@PathVariable Long id) {
        return clienteService.obtenerCliente(id);
    }
}
