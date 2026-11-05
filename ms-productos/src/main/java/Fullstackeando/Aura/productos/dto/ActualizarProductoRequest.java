package Fullstackeando.Aura.productos.dto;

import jakarta.validation.constraints.*;

public record ActualizarProductoRequest(@Positive Integer precio, @PositiveOrZero Integer stock) {}
