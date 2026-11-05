package Fullstackeando.Aura.productos.dto;

import jakarta.validation.constraints.*;

public record ItemStockRequest(@NotBlank @Size(max = 10) String codigo, @NotNull @Positive Integer cantidad) {}
