package Fullstackeando.Aura.pagos.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//los datos del cliente que responde ms-usuarios
//solo se piden los que necesita pagos para revisar la cuenta corriente y la deuda

//se usa porque usuarios responde con un json y java necesita un clase para convertir ese
//json en un objeto que pueda usar
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClienteRemoto {

    private Long id;
    private boolean cuentaCorrienteHabilitada;
    private int saldoAdeudado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public boolean isCuentaCorrienteHabilitada() { return cuentaCorrienteHabilitada; }
    public void setCuentaCorrienteHabilitada(boolean cuentaCorrienteHabilitada) { this.cuentaCorrienteHabilitada = cuentaCorrienteHabilitada; }
    public int getSaldoAdeudado() { return saldoAdeudado; }
    public void setSaldoAdeudado(int saldoAdeudado) { this.saldoAdeudado = saldoAdeudado; }
}
