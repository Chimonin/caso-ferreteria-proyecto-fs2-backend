package fullstackeando.aura.productos.controller;

import fullstackeando.aura.productos.dto.ProductoResponse;
import fullstackeando.aura.productos.service.ProductoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    //GET /api/productos -> catalogo publico
    @GetMapping
    public List<ProductoResponse> listarProductos() {
        return productoService.listarProductos();
    }

    //GET /api/productos/MC001 -> detalle del producto
    @GetMapping("/{codigo}")
    public ProductoResponse obtenerProducto(@PathVariable String codigo) {
        return productoService.obtenerProducto(codigo);
    }
}
