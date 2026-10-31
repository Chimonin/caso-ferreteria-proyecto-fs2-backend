package Fullstackeando.Aura.pagos.repository;

import Fullstackeando.Aura.pagos.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    //SELECT * FROM pago WHERE cliente_id = ?
    List<Pago> findByClienteId(Long clienteId);
}
