package Fullstackeando.Aura.pagos.dto;

import jakarta.validation.constraints.*;

//viene de mi credito
public class PagoRequest {

    @NotNull(message = "Cliente no encontrado")
    private Long clienteId;

    @NotNull(message = "El monto debe ser un número entero mayor a 0")
    @Min(value = 1, message = "El monto debe ser un número entero mayor a 0")
    private Integer monto;

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public Integer getMonto() { return monto; }
    public void setMonto(Integer monto) { this.monto = monto; }
}
