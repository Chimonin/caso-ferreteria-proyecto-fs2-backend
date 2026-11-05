package Fullstackeando.Aura.productos.controller;

import Fullstackeando.Aura.productos.dto.*;
import Fullstackeando.Aura.productos.service.ProductoGestionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/internal/productos")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "internalKey")
public class ProductoInternoController {
    private final ProductoGestionService gestion;
    public ProductoInternoController(ProductoGestionService gestion) { this.gestion = gestion; }

    @PostMapping("/descontar-stock")
    public List<ProductoDescontado> descontarStock(@RequestBody @NotEmpty List<@NotNull @Valid ItemStockRequest> items) {
        return gestion.descontarStock(items);
    }

    @PostMapping("/reponer-stock")
    public ResponseEntity<Void> reponerStock(@RequestBody @NotEmpty List<@NotNull @Valid ItemStockRequest> items) {
        gestion.reponerStock(items);
        return ResponseEntity.noContent().build();
    }
}
