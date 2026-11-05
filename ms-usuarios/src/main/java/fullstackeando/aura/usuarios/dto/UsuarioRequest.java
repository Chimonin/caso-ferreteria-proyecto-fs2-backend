package fullstackeando.aura.usuarios.dto;

import jakarta.validation.constraints.*;

public record UsuarioRequest(
    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(regexp = "[0-9]{6,8}[0-9kK]", message = "El RUT debe tener entre 7 y 9 caracteres, sin puntos ni guion")
    String rut,
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    String nombre,
    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres")
    String apellidos,
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe ser @duoc.cl, @profesor.duoc.cl o @gmail.com")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
    String correo,
    String clave,
    @NotBlank(message = "Selecciona un rol válido")
    @Pattern(regexp = "admin|vendedor|cliente", message = "Selecciona un rol válido")
    String rol,
    @NotBlank(message = "Selecciona una región")
    @Size(max = 60, message = "Selecciona una región")
    String region,
    @NotBlank(message = "Selecciona una comuna")
    @Size(max = 60, message = "Selecciona una comuna")
    String comuna,
    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 300, message = "La dirección no puede superar los 300 caracteres")
    String direccion,
    String tipoCliente,
    Boolean cuentaCorrienteHabilitada) {
}
