package fullstackeando.aura.usuarios.service;

import fullstackeando.aura.usuarios.dto.UsuarioResponse;
import fullstackeando.aura.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findByActivoTrue().stream()
                .map(UsuarioResponse::new)
                .toList();
    }

    public UsuarioResponse obtenerUsuario(Long id) {
        return usuarioRepository.findByIdAndActivoTrue(id)
                .map(UsuarioResponse::new)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }
}
