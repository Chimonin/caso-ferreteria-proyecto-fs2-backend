package fullstackeando.aura.productos.service;

import fullstackeando.aura.productos.dto.ProductoResponse;
import fullstackeando.aura.productos.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    //el catalogo es publico y solo incluye productos activos
    public List<ProductoResponse> listarProductos() {
        return productoRepository.findByActivoTrue().stream()
                .map(ProductoResponse::new)
                .toList();
    }

    public ProductoResponse obtenerProducto(String codigo) {
        return productoRepository.findByCodigoAndActivoTrue(codigo)
                .map(ProductoResponse::new)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado"));
    }
}
