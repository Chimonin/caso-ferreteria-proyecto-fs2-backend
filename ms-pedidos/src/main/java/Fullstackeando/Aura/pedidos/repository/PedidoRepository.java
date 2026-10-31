package Fullstackeando.Aura.pedidos.repository;

import Fullstackeando.Aura.pedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    //ya viene incluido los principales metodos de crud

    //SELECT * FROM pedido WHERE cliente_id = ?
    //clienteId es el parametro que reemplaza el ?
    List<Pedido> findByClienteId(Long clienteId);
}
