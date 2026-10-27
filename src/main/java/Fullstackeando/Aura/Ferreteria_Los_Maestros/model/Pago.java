package Fullstackeando.Aura.Ferreteria_Los_Maestros.model;

import jakarta.persistence.*;
import java.time.LocalDate;

//un pago es lo que un cliente abona a la deuda de su cuenta corriente
@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clienteId;

    private LocalDate fecha;
    private int monto;

    // getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public int getMonto() { return monto; }
    public void setMonto(int monto) { this.monto = monto; }
}
