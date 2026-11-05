package fullstackeando.aura.usuarios.dto;

import jakarta.validation.constraints.*;

public record ClienteRequest(
    @NotBlank @Pattern(regexp = "particular|contratista") String tipoCliente,
    @NotNull Boolean cuentaCorrienteHabilitada) {
}
