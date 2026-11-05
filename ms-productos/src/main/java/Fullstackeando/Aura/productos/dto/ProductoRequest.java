package Fullstackeando.Aura.productos.dto;

import jakarta.validation.constraints.*;

public record ProductoRequest(
    @NotBlank @Size(max = 10) String codigo,
    @NotBlank @Size(max = 150) String nombre,
    @NotBlank @Size(max = 50) String categoria,
    @NotBlank @Size(max = 50) String subcategoria,
    @NotBlank @Size(max = 50) String marca,
    @NotBlank @Size(max = 30) String unidad,
    @NotNull @Positive Integer precio,
    @NotNull @PositiveOrZero Integer stock,
    @NotNull @PositiveOrZero Integer stockMinimo) {}
