package Fullstackeando.Aura.pedidos.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//lo que responde ms-productos por cada item al descontar el stock
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductoDescontado {

    private String codigo;
    private String nombre;
    private int precio;
    private int cantidad;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getPrecio() { return precio; }
    public void setPrecio(int precio) { this.precio = precio; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
