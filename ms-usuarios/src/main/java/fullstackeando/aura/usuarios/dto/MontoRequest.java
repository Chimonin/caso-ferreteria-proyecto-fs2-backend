package Fullstackeando.Aura.usuarios.dto;

import jakarta.validation.constraints.*;

public record MontoRequest(
    @NotNull(message = "El monto debe ser un número entero mayor a 0")
    @Positive(message = "El monto debe ser un número entero mayor a 0")
    Integer monto) {}
