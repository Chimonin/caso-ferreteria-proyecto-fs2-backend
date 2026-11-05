package Fullstackeando.Aura.usuarios.dto;

import jakarta.validation.constraints.*;

public record RegistroRequest(
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
    @NotBlank(message = "La contraseña debe tener entre 4 y 10 caracteres")
    @Size(min = 4, max = 10, message = "La contraseña debe tener entre 4 y 10 caracteres")
    String clave,
    @NotBlank(message = "Selecciona una región")
    @Size(max = 60, message = "Selecciona una región")
    String region,
    @NotBlank(message = "Selecciona una comuna")
    @Size(max = 60, message = "Selecciona una comuna")
    String comuna,
    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 300, message = "La dirección no puede superar los 300 caracteres")
    String direccion,
    @NotBlank(message = "Selecciona si el cliente es particular o contratista")
    @Pattern(regexp = "particular|contratista", message = "Selecciona si el cliente es particular o contratista")
    String tipoCliente) {
    public UsuarioRequest comoUsuario() {
        return new UsuarioRequest(rut, nombre, apellidos, correo, clave, "cliente",
                region, comuna, direccion, tipoCliente, false);
    }
}
