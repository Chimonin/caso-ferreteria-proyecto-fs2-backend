package fullstackeando.aura.usuarios.dto;

import jakarta.validation.constraints.*;

//el registro publico nunca recibe un rol ni habilita credito
public record RegistroRequest(
    @NotBlank @Pattern(regexp = "[0-9]{6,8}[0-9kK]") String rut,
    @NotBlank @Size(max = 50) String nombre,
    @NotBlank @Size(max = 100) String apellidos,
    @NotBlank @Email @Size(max = 100) String correo,
    @NotBlank @Size(min = 4, max = 10) String clave,
    @NotBlank @Size(max = 60) String region,
    @NotBlank @Size(max = 60) String comuna,
    @NotBlank @Size(max = 300) String direccion,
    @NotBlank @Pattern(regexp = "particular|contratista") String tipoCliente) {
    public UsuarioRequest comoUsuario() {
        return new UsuarioRequest(rut, nombre, apellidos, correo, clave, "cliente",
                region, comuna, direccion, tipoCliente, false);
    }
}
