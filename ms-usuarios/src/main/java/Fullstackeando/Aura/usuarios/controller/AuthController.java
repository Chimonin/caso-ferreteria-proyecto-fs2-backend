package Fullstackeando.Aura.usuarios.controller;

import Fullstackeando.Aura.usuarios.dto.*;
import Fullstackeando.Aura.usuarios.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import Fullstackeando.Aura.usuarios.service.UsuarioGestionService;
import org.springframework.http.ResponseEntity;
import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UsuarioGestionService gestion;

    public AuthController(AuthService authService, UsuarioGestionService gestion) {
        this.authService = authService; this.gestion = gestion;
    }

    @PostMapping("/login")
    @SecurityRequirements
    public AuthResponse iniciarSesion(@Valid @RequestBody LoginRequest request) {
        return authService.iniciarSesion(request);
    }

    @PostMapping("/registro")
    @SecurityRequirements
    public ResponseEntity<AuthResponse> registrarCliente(@Valid @RequestBody RegistroRequest datos) {
        var usuario = gestion.crearUsuario(datos.comoUsuario());
        var respuesta = authService.iniciarSesion(new LoginRequest(datos.correo(), datos.clave()));
        return ResponseEntity.created(URI.create("/api/clientes/" + usuario.getId())).body(respuesta);
    }
}
