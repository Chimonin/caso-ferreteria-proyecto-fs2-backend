//reglas de negocio de los pagos, las mismas de pagoService.js del frontend
package Fullstackeando.Aura.Ferreteria_Los_Maestros.service;

import Fullstackeando.Aura.Ferreteria_Los_Maestros.exception.ReglaNegocioException;
import Fullstackeando.Aura.Ferreteria_Los_Maestros.model.Cliente;
import Fullstackeando.Aura.Ferreteria_Los_Maestros.model.Pago;
import Fullstackeando.Aura.Ferreteria_Los_Maestros.repository.ClienteRepository;
import Fullstackeando.Aura.Ferreteria_Los_Maestros.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final ClienteRepository clienteRepository;

    public PagoService(PagoRepository pagoRepository, ClienteRepository clienteRepository) {
        this.pagoRepository = pagoRepository;
        this.clienteRepository = clienteRepository;
    }

    public List<Pago> listarPagos() {
        return pagoRepository.findAll();
    }

    public List<Pago> listarPagosCliente(Long clienteId) {
        return pagoRepository.findByClienteId(clienteId);
    }

    //el cliente paga su deuda
    @Transactional
    public Pago registrarPago(Long clienteId, int monto) {
        if (monto <= 0) {
            throw new ReglaNegocioException("El monto debe ser un número entero mayor a 0");
        }

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado"));

        if (!cliente.isCuentaCorrienteHabilitada()) {
            throw new ReglaNegocioException("El cliente no tiene cuenta corriente habilitada");
        }
        if (monto > cliente.getSaldoAdeudado()) {
            throw new ReglaNegocioException("El monto no puede ser mayor a la deuda");
        }

        Pago nuevo = new Pago();
        nuevo.setClienteId(clienteId);
        nuevo.setFecha(LocalDate.now());
        nuevo.setMonto(monto);
        pagoRepository.save(nuevo);

        cliente.setSaldoAdeudado(cliente.getSaldoAdeudado() - monto);
        clienteRepository.save(cliente);

        return nuevo;
    }
}
