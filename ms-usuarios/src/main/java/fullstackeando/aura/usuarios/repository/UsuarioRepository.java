package fullstackeando.aura.usuarios.repository;

import fullstackeando.aura.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    Optional<Usuario> findByRutIgnoreCase(String rut);

    //serializa los cambios de roles, bajas y deuda para conservar las reglas entre peticiones
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from Usuario u order by u.id")
    List<Usuario> bloquearUsuarios();
    Optional<Usuario> findByCorreoIgnoreCaseAndActivoTrue(String correo);
    List<Usuario> findByActivoTrue();
    Optional<Usuario> findByIdAndActivoTrue(Long id);
}
