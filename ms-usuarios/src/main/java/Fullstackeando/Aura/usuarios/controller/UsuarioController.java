package Fullstackeando.Aura.usuarios.controller;

import Fullstackeando.Aura.usuarios.dto.UsuarioResponse;
import Fullstackeando.Aura.usuarios.service.UsuarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import Fullstackeando.Aura.usuarios.dto.UsuarioRequest;
import Fullstackeando.Aura.usuarios.service.UsuarioGestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import java.net.URI;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioGestionService gestion;

    public UsuarioController(UsuarioService usuarioService, UsuarioGestionService gestion) {
        this.usuarioService = usuarioService;
        this.gestion = gestion;
    }

    @GetMapping
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtenerUsuario(@PathVariable Long id) {
        return usuarioService.obtenerUsuario(id);
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody UsuarioRequest datos) {
        var usuario = gestion.crearUsuario(datos);
        return ResponseEntity.created(URI.create("/api/usuarios/" + usuario.getId())).body(usuario);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioRequest datos) {
        return gestion.actualizarUsuario(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        gestion.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
