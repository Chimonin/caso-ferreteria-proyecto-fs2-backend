package fullstackeando.aura.usuarios.controller;

import fullstackeando.aura.usuarios.dto.*;
import fullstackeando.aura.usuarios.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/login")
    @SecurityRequirements
    public AuthResponse iniciarSesion(@Valid @RequestBody LoginRequest request) {
        return authService.iniciarSesion(request);
    }
}
