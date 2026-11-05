package Fullstackeando.Aura.productos.dto;

import jakarta.validation.constraints.*;

public record StockRequest(@NotNull @PositiveOrZero Integer stock) {}
