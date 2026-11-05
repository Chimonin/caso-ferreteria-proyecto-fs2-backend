package fullstackeando.aura.usuarios.repository;

import fullstackeando.aura.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoIgnoreCaseAndActivoTrue(String correo);
    List<Usuario> findByActivoTrue();
    Optional<Usuario> findByIdAndActivoTrue(Long id);
}
