package Fullstackeando.Aura.productos.controller;

import Fullstackeando.Aura.productos.dto.ProductoResponse;
import Fullstackeando.Aura.productos.service.ProductoService;
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
