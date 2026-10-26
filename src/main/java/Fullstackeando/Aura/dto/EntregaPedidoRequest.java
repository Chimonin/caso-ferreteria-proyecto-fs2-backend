package Fullstackeando.Aura.Ferreteria_Los_Maestros.dto;

import jakarta.validation.constraints.*;

//los datosEntrega que arma Envios.jsx: modalidad, comuna y direccion
public class EntregaPedidoRequest {

    @NotBlank(message = "Selecciona una modalidad de entrega.")
    @Pattern(regexp = "retiro|despacho", message = "Selecciona una modalidad de entrega válida.")
    private String modalidad;

    @Size(max = 60, message = "La comuna no puede tener más de 60 caracteres.")
    private String comuna;

    @Size(max = 300, message = "La dirección no puede tener más de 300 caracteres.")
    private String direccion;

    //validacion condicional: si es despacho, comuna y direccion son obligatorias
    //si es retiro no se revisan
    @AssertTrue(message = "Completa la comuna y la dirección para el despacho.")
    public boolean isDireccionCompleta() {
        if (!"despacho".equals(modalidad)) return true;
        return comuna != null && !comuna.isBlank()
                && direccion != null && !direccion.isBlank();
    }

    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }
    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
