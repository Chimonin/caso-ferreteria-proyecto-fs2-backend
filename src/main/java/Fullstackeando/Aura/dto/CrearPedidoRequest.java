package Fullstackeando.Aura.Ferreteria_Los_Maestros.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

//cliente id, medio pago, entregapedidorequest y itemscarritorequest
public class CrearPedidoRequest {

    @NotNull(message = "Inicia sesión como cliente para comprar.")
    private Long clienteId;

    @NotBlank(message = "Selecciona un medio de pago válido.")
    //pattern valida que un texto tenga una forma especifica
    //regexp es la forma escrita como expresion regular
    @Pattern(regexp = "contado|cuenta_corriente", message = "Selecciona un medio de pago válido.")
    private String medioPago;

    //@Valid revisa tambien las validaciones dentro de este objeto
    @NotNull(message = "Selecciona una modalidad de entrega.")
    @Valid
    private EntregaPedidoRequest entrega;

    @NotEmpty(message = "El carrito está vacío.")
    @Valid
    private List<ItemCarritoRequest> items;

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getMedioPago() { return medioPago; }
    public void setMedioPago(String medioPago) { this.medioPago = medioPago; }
    public EntregaPedidoRequest getEntrega() { return entrega; }
    public void setEntrega(EntregaPedidoRequest entrega) { this.entrega = entrega; }
    public List<ItemCarritoRequest> getItems() { return items; }
    public void setItems(List<ItemCarritoRequest> items) { this.items = items; }
}
