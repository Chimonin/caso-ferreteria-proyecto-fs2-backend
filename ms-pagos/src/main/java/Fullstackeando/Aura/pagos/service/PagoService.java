//reglas de negocio de los pagos, las mismas de pagoService.js del frontend
package Fullstackeando.Aura.pagos.service;

import Fullstackeando.Aura.pagos.client.ClienteClient;
import Fullstackeando.Aura.pagos.client.ClienteRemoto;
import Fullstackeando.Aura.pagos.exception.ReglaNegocioException;
import Fullstackeando.Aura.pagos.model.Pago;
import Fullstackeando.Aura.pagos.repository.PagoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);

    private final PagoRepository pagoRepository;
    private final ClienteClient clienteClient;

    public PagoService(PagoRepository pagoRepository, ClienteClient clienteClient) {
        this.pagoRepository = pagoRepository;
        this.clienteClient = clienteClient;
    }

    public List<Pago> listarPagos() {
        return pagoRepository.findAll();
    }

    public List<Pago> listarPagosCliente(Long clienteId) {
        return pagoRepository.findByClienteId(clienteId);
    }

    //el cliente paga su deuda
    //@Transactional solo cubre la base de pagos: la deuda esta en ms-usuarios
    @Transactional
    public Pago registrarPago(Long clienteId, int monto) {
        if (monto <= 0) {
            throw new ReglaNegocioException("El monto debe ser un número entero mayor a 0");
        }

        ClienteRemoto cliente = clienteClient.obtenerCliente(clienteId);

        if (!cliente.isCuentaCorrienteHabilitada()) {
            throw new ReglaNegocioException("El cliente no tiene cuenta corriente habilitada");
        }
        if (monto > cliente.getSaldoAdeudado()) {
            throw new ReglaNegocioException("El monto no puede ser mayor a la deuda");
        }

        clienteClient.restarDeuda(clienteId, monto);

        //3. guarda el pago en la base de pagos
        //si falla, se devuelve la deuda (compensacion)
        try {
            Pago nuevo = new Pago();
            nuevo.setClienteId(clienteId);
            nuevo.setFecha(LocalDate.now());
            nuevo.setMonto(monto);
            return pagoRepository.saveAndFlush(nuevo);
        } catch (RuntimeException e) {
            try {
                clienteClient.sumarDeuda(clienteId, monto);
            } catch (RuntimeException errorCompensacion) {
                log.error("No se pudo devolver la deuda del cliente {}: {}", clienteId, errorCompensacion.getMessage());
            }
            throw e;
        }
    }
}
