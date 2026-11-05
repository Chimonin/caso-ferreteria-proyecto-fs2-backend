package Fullstackeando.Aura.productos.dto;

import jakarta.validation.constraints.*;

public record ProductoRequest(
    @NotBlank(message = "Todos los campos son obligatorios")
    @Size(max = 10, message = "El código no puede superar los 10 caracteres")
    String codigo,
    @NotBlank(message = "Todos los campos son obligatorios")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    String nombre,
    @NotBlank(message = "Todos los campos son obligatorios")
    @Size(max = 50, message = "La categoría no puede superar los 50 caracteres")
    String categoria,
    @NotBlank(message = "Todos los campos son obligatorios")
    @Size(max = 50, message = "La subcategoría no puede superar los 50 caracteres")
    String subcategoria,
    @NotBlank(message = "Todos los campos son obligatorios")
    @Size(max = 50, message = "La marca no puede superar los 50 caracteres")
    String marca,
    @NotBlank(message = "Todos los campos son obligatorios")
    @Size(max = 30, message = "La unidad no puede superar los 30 caracteres")
    String unidad,
    @NotNull(message = "Todos los campos son obligatorios")
    @Positive(message = "El precio debe ser un número entero mayor a 0")
    Integer precio,
    @NotNull(message = "Todos los campos son obligatorios")
    @PositiveOrZero(message = "La cantidad debe ser un número entero de 0 o más")
    Integer stock,
    @NotNull(message = "Todos los campos son obligatorios")
    @PositiveOrZero(message = "El stock mínimo debe ser un número entero de 0 o más")
    Integer stockMinimo) {}
