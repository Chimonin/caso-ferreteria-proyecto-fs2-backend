package Fullstackeando.Aura.Ferreteria_Los_Maestros.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {

    //lista de estados permitidos

    //java.util.list crea una lista fija que no se puede modificar
    public static final List<String> ESTADOS_PEDIDO =
            List.of("Pendiente", "En preparación", "Despachado", "Entregado");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clienteId;

    private LocalDate fecha;

    private String estado;

    private String medioPago;

    private String modalidadEntrega;

    private String comunaEntrega;

    private String direccionEntrega;

    private int costoEnvio;

    private int total;

    //cascade all: al guardar el pedido tambien se guardan sus items
    //primero guarda el pedido, luego sus items, porque requieren el pedido id

    //java.util.arraylist crea una lista que si se puede modificar
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<DetallePedido> items = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMedioPago() { return medioPago; }
    public void setMedioPago(String medioPago) { this.medioPago = medioPago; }
    public String getModalidadEntrega() { return modalidadEntrega; }
    public void setModalidadEntrega(String modalidadEntrega) { this.modalidadEntrega = modalidadEntrega; }
    public String getComunaEntrega() { return comunaEntrega; }
    public void setComunaEntrega(String comunaEntrega) { this.comunaEntrega = comunaEntrega; }
    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }
    public int getCostoEnvio() { return costoEnvio; }
    public void setCostoEnvio(int costoEnvio) { this.costoEnvio = costoEnvio; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public List<DetallePedido> getItems() { return items; }
    public void setItems(List<DetallePedido> items) { this.items = items; }
}
