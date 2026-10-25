package Fullstackeando.Aura.Ferreteria_Los_Maestros.repository;

import Fullstackeando.Aura.Ferreteria_Los_Maestros.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    //ya viene incluido save(pedido), findAll(), findById(id), existsById(id)

    //spring arma la consulta solo por el nombre del metodo:
    //SELECT * FROM pedido WHERE cliente_id = ?
    List<Pedido> findByClienteId(Long clienteId);
}
