package fullstackeando.aura.productos.controller;

import fullstackeando.aura.productos.dto.ProductoResponse;
import fullstackeando.aura.productos.service.ProductoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import fullstackeando.aura.productos.dto.*;
import fullstackeando.aura.productos.service.ProductoGestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import java.net.URI;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoGestionService gestion;

    public ProductoController(ProductoService productoService, ProductoGestionService gestion) {
        this.productoService = productoService;
        this.gestion = gestion;
    }

    //GET /api/productos -> catalogo publico
    @GetMapping
    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    public List<ProductoResponse> listarProductos() {
        return productoService.listarProductos();
    }

    //GET /api/productos/MC001 -> detalle del producto
    @GetMapping("/{codigo}")
    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    public ProductoResponse obtenerProducto(@PathVariable String codigo) {
        return productoService.obtenerProducto(codigo);
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> crearProducto(@Valid @RequestBody ProductoRequest datos) {
        var producto = gestion.crearProducto(datos);
        return ResponseEntity.created(URI.create("/api/productos/" + producto.getCodigo())).body(producto);
    }

    @PatchMapping("/{codigo}")
    public ProductoResponse actualizarProducto(@PathVariable String codigo, @Valid @RequestBody ActualizarProductoRequest datos) {
        return gestion.actualizarProducto(codigo, datos);
    }

    @PutMapping("/{codigo}")
    public ProductoResponse reemplazarProducto(@PathVariable String codigo, @Valid @RequestBody ProductoRequest datos) {
        return gestion.reemplazarProducto(codigo, datos);
    }

    @PatchMapping("/{codigo}/stock")
    public ProductoResponse actualizarStock(@PathVariable String codigo, @Valid @RequestBody StockRequest datos) {
        return gestion.actualizarProducto(codigo, new ActualizarProductoRequest(null, datos.stock()));
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable String codigo) {
        gestion.eliminarProducto(codigo);
        return ResponseEntity.noContent().build();
    }
}
