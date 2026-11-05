package fullstackeando.aura.usuarios.repository;

import fullstackeando.aura.usuarios.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByUsuarioActivoTrue();
    Optional<Cliente> findByIdAndUsuarioActivoTrue(Long id);
}
