package Fullstackeando.Aura.usuarios.dto;

import jakarta.validation.constraints.*;

public record MontoRequest(@NotNull @Positive Integer monto) {}
