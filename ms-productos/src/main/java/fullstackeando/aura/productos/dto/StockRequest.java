package Fullstackeando.Aura.productos.dto;

import jakarta.validation.constraints.*;

public record StockRequest(
    @NotNull(message = "Ingresa una cantidad")
    @PositiveOrZero(message = "La cantidad debe ser un número entero de 0 o más")
    Integer stock) {}
