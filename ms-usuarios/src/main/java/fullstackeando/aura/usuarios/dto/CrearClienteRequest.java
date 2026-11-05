package fullstackeando.aura.usuarios.dto;

import jakarta.validation.constraints.*;

public record CrearClienteRequest(
    @NotNull(message = "Selecciona un usuario")
    @Positive(message = "Selecciona un usuario")
    Long id,
    @NotBlank(message = "Selecciona si el cliente es particular o contratista")
    @Pattern(regexp = "particular|contratista", message = "Selecciona si el cliente es particular o contratista")
    String tipoCliente,
    @NotNull(message = "Indica si el cliente tiene cuenta corriente habilitada")
    Boolean cuentaCorrienteHabilitada) {
    public ClienteRequest datos() { return new ClienteRequest(tipoCliente, cuentaCorrienteHabilitada); }
}
