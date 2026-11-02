package Fullstackeando.Aura.productos.repository;

import Fullstackeando.Aura.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, String> {
    //los metodos de guardar, buscar por codigo y verificar si existe ya vienen incluidos

    //el catalogo solo muestra los productos que no han sido dados de baja
    List<Producto> findByActivoTrue();

    //busca un producto disponible en el catalogo usando su codigo
    Optional<Producto> findByCodigoAndActivoTrue(String codigo);
}
