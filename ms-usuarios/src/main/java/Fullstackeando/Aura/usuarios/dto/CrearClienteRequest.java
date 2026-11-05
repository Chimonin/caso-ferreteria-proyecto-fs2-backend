package Fullstackeando.Aura.usuarios.dto;

import jakarta.validation.constraints.*;

public record CrearClienteRequest(
    @NotNull @Positive Long id,
    @NotBlank @Pattern(regexp = "particular|contratista") String tipoCliente,
    @NotNull Boolean cuentaCorrienteHabilitada) {
    public ClienteRequest datos() { return new ClienteRequest(tipoCliente, cuentaCorrienteHabilitada); }
}
