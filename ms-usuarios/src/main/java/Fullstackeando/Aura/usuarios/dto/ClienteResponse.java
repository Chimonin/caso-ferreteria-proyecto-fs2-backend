package Fullstackeando.Aura.usuarios.dto;

import Fullstackeando.Aura.usuarios.model.Cliente;
import Fullstackeando.Aura.usuarios.model.Usuario;

//datos de salida para las vistas, sin incluir la clave
public class ClienteResponse {

    private final Long id;
    private final String rut;
    private final String nombre;
    private final String apellidos;
    private final String nombreCompleto;
    private final String correo;
    private final String region;
    private final String comuna;
    private final String direccion;
    private final String tipoCliente;
    private final boolean cuentaCorrienteHabilitada;
    private final int saldoAdeudado;

    public ClienteResponse(Cliente cliente) {
        Usuario usuario = cliente.getUsuario();
        id = usuario.getId();
        rut = usuario.getRut();
        nombre = usuario.getNombre();
        apellidos = usuario.getApellidos();
        nombreCompleto = usuario.getNombre() + " " + usuario.getApellidos();
        correo = usuario.getCorreo();
        region = usuario.getRegion();
        comuna = usuario.getComuna();
        direccion = usuario.getDireccion();
        tipoCliente = cliente.getTipoCliente();
        cuentaCorrienteHabilitada = cliente.isCuentaCorrienteHabilitada();
        saldoAdeudado = cliente.getSaldoAdeudado();
    }

    public Long getId() { return id; }
    public String getRut() { return rut; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getCorreo() { return correo; }
    public String getRegion() { return region; }
    public String getComuna() { return comuna; }
    public String getDireccion() { return direccion; }
    public String getTipoCliente() { return tipoCliente; }
    public boolean isCuentaCorrienteHabilitada() { return cuentaCorrienteHabilitada; }
    public int getSaldoAdeudado() { return saldoAdeudado; }
}
