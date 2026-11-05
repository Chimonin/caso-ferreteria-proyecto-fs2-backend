package fullstackeando.aura.usuarios.dto;

//usuario contiene los datos que ya usan las vistas, nunca la clave
public record AuthResponse(String token, String tipoToken, long expiresIn, UsuarioResponse usuario) {
}
