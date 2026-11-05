package Fullstackeando.Aura.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Ingresa tu correo") @Email(message = "Ingresa un correo válido") String correo,
        @NotBlank(message = "Ingresa tu contraseña") String clave) {
}
