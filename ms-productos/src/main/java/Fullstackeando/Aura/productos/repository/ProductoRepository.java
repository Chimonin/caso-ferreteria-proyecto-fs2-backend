package Fullstackeando.Aura.productos.repository;

import Fullstackeando.Aura.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, String> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from Producto p where p.codigo = :codigo")
    Optional<Producto> bloquearPorCodigo(String codigo);
    //los metodos de guardar, buscar por codigo y verificar si existe ya vienen incluidos

    //el catalogo solo muestra los productos que no han sido dados de baja
    List<Producto> findByActivoTrue();

    //busca un producto disponible en el catalogo usando su codigo
    Optional<Producto> findByCodigoAndActivoTrue(String codigo);
}
