package Fullstackeando.Aura.Ferreteria_Los_Maestros.dto;

import jakarta.validation.constraints.*;

public class ItemCarritoRequest {

    @NotBlank(message = "Falta el código del producto.")
    private String codigo;

    @Min(value = 1, message = "La cantidad debe ser un número entero mayor a 0.")
    private int cantidad;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
