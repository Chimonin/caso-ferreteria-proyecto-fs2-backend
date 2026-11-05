package fullstackeando.aura.usuarios.dto;

import jakarta.validation.constraints.*;

public record UsuarioRequest(
    @NotBlank @Pattern(regexp = "[0-9]{6,8}[0-9kK]") String rut,
    @NotBlank @Size(max = 50) String nombre,
    @NotBlank @Size(max = 100) String apellidos,
    @NotBlank @Email @Size(max = 100) String correo,
    String clave,
    @NotBlank @Pattern(regexp = "admin|vendedor|cliente") String rol,
    @NotBlank @Size(max = 60) String region,
    @NotBlank @Size(max = 60) String comuna,
    @NotBlank @Size(max = 300) String direccion,
    String tipoCliente,
    Boolean cuentaCorrienteHabilitada) {
}
