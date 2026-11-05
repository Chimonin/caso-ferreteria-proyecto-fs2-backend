package fullstackeando.aura.usuarios.dto;

import fullstackeando.aura.usuarios.model.Usuario;

//datos de salida para las vistas, sin incluir la clave
public class UsuarioResponse {

    private final Long id;
    private final String rut;
    private final String nombre;
    private final String apellidos;
    private final String nombreCompleto;
    private final String correo;
    private final String rol;
    private final String region;
    private final String comuna;
    private final String direccion;

    public UsuarioResponse(Usuario usuario) {
        id = usuario.getId();
        rut = usuario.getRut();
        nombre = usuario.getNombre();
        apellidos = usuario.getApellidos();
        nombreCompleto = usuario.getNombre() + " " + usuario.getApellidos();
        correo = usuario.getCorreo();
        rol = usuario.getRol();
        region = usuario.getRegion();
        comuna = usuario.getComuna();
        direccion = usuario.getDireccion();
    }

    public Long getId() { return id; }
    public String getRut() { return rut; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getCorreo() { return correo; }
    public String getRol() { return rol; }
    public String getRegion() { return region; }
    public String getComuna() { return comuna; }
    public String getDireccion() { return direccion; }
}
