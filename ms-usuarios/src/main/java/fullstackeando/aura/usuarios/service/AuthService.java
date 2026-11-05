package fullstackeando.aura.usuarios.service;

import fullstackeando.aura.usuarios.dto.*;
import fullstackeando.aura.usuarios.security.*;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
    }

    public AuthResponse iniciarSesion(LoginRequest request) {
        var autenticacion = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.correo().trim(), request.clave()));
        var usuario = (UsuarioAutenticado) autenticacion.getPrincipal();
        return new AuthResponse(jwtService.generarToken(usuario), "Bearer", jwtService.getExpirationMs() / 1000,
                usuarioService.obtenerUsuario(usuario.getId()));
    }
}
