package fullstackeando.aura.usuarios.dto;

import jakarta.validation.constraints.*;

public record ClienteRequest(
    @NotBlank(message = "Selecciona si el cliente es particular o contratista")
    @Pattern(regexp = "particular|contratista", message = "Selecciona si el cliente es particular o contratista")
    String tipoCliente,
    @NotNull(message = "Indica si el cliente tiene cuenta corriente habilitada")
    Boolean cuentaCorrienteHabilitada) {
}
