package Fullstackeando.Aura.usuarios.service;

import Fullstackeando.Aura.usuarios.repository.UsuarioRepository;
import Fullstackeando.Aura.usuarios.security.UsuarioAutenticado;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UsuarioAutenticado loadUserByUsername(String correo) {
        return usuarioRepository.findByCorreoIgnoreCaseAndActivoTrue(correo.trim())
                .map(UsuarioAutenticado::new)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales incorrectas"));
    }
}
