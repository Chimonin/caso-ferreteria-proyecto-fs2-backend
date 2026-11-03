package Fullstackeando.Aura.usuarios.controller;

import Fullstackeando.Aura.usuarios.dto.UsuarioResponse;
import Fullstackeando.Aura.usuarios.service.UsuarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtenerUsuario(@PathVariable Long id) {
        return usuarioService.obtenerUsuario(id);
    }
}
