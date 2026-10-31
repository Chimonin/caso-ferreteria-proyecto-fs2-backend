package Fullstackeando.Aura.pedidos.dto;

import jakarta.validation.constraints.NotBlank;

public class CambiarEstadoPedidoRequest {

    @NotBlank(message = "Estado de pedido no válido")
    private String estado;

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
