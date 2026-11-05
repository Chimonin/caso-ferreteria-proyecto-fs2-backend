package Fullstackeando.Aura.productos.dto;

import jakarta.validation.constraints.*;

public record ActualizarProductoRequest(
    @Positive(message = "El precio debe ser un número entero mayor a 0") Integer precio,
    @PositiveOrZero(message = "La cantidad debe ser un número entero de 0 o más") Integer stock) {}
