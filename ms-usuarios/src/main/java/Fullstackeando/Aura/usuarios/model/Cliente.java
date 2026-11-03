package Fullstackeando.Aura.usuarios.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cliente")
public class Cliente {

    //el cliente usa el mismo id de su usuario
    @Id
    @Column(name = "usuario_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 20)
    private String tipoCliente;

    @Column(nullable = false)
    private boolean cuentaCorrienteHabilitada;

    @Column(nullable = false)
    private int saldoAdeudado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(String tipoCliente) { this.tipoCliente = tipoCliente; }
    public boolean isCuentaCorrienteHabilitada() { return cuentaCorrienteHabilitada; }
    public void setCuentaCorrienteHabilitada(boolean cuentaCorrienteHabilitada) { this.cuentaCorrienteHabilitada = cuentaCorrienteHabilitada; }
    public int getSaldoAdeudado() { return saldoAdeudado; }
    public void setSaldoAdeudado(int saldoAdeudado) { this.saldoAdeudado = saldoAdeudado; }
}
