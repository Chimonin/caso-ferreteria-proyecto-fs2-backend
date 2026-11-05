package Fullstackeando.Aura.usuarios.service;

import Fullstackeando.Aura.usuarios.dto.*;
import Fullstackeando.Aura.usuarios.model.*;
import Fullstackeando.Aura.usuarios.repository.*;
import Fullstackeando.Aura.usuarios.exception.ReglaNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;

@Service
@Transactional
public class ClienteGestionService {
    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;

    public ClienteGestionService(UsuarioRepository usuarios, ClienteRepository clientes) {
        this.usuarios = usuarios;
        this.clientes = clientes;
    }

    public ClienteResponse crearCliente(CrearClienteRequest datos) {
        Usuario usuario = bloquearUsuario(datos.id());
        if (clientes.existsById(datos.id())) throw new ReglaNegocioException("Este usuario ya es cliente");
        if (!usuario.getRol().equals("cliente")) throw new ReglaNegocioException("El usuario debe tener rol cliente");
        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        configurar(cliente, datos.datos());
        return new ClienteResponse(clientes.saveAndFlush(cliente));
    }

    public ClienteResponse actualizarCliente(Long id, ClienteRequest datos) {
        bloquearUsuario(id);
        Cliente cliente = buscarCliente(id);
        configurar(cliente, datos);
        return new ClienteResponse(cliente);
    }

    public void eliminarCliente(Long id) {
        Usuario usuario = bloquearUsuario(id);
        Cliente cliente = buscarCliente(id);
        if (cliente.getSaldoAdeudado() > 0)
            throw new ReglaNegocioException("El cliente tiene deuda pendiente en su cuenta corriente");
        usuario.setActivo(false);
    }

    public void cambiarDeuda(Long id, int monto, boolean sumar) {
        if (monto <= 0) throw new ReglaNegocioException("El monto debe ser mayor a 0");
        bloquearUsuario(id);
        Cliente cliente = buscarCliente(id);
        if (!cliente.isCuentaCorrienteHabilitada())
            throw new ReglaNegocioException("El cliente no tiene cuenta corriente habilitada");
        if (!sumar && monto > cliente.getSaldoAdeudado())
            throw new ReglaNegocioException("El monto no puede ser mayor a la deuda");
        long nuevo = (long) cliente.getSaldoAdeudado() + (sumar ? monto : -monto);
        if (nuevo > Integer.MAX_VALUE) throw new ReglaNegocioException("El saldo excede el máximo permitido");
        cliente.setSaldoAdeudado((int) nuevo);
    }

    private Usuario bloquearUsuario(Long id) {
        return usuarios.bloquearUsuarios().stream().filter(u -> u.getId().equals(id) && u.isActivo()).findFirst()
                .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado"));
    }

    private Cliente buscarCliente(Long id) {
        return clientes.findById(id).orElseThrow(() -> new NoSuchElementException("Cliente no encontrado"));
    }

    private void configurar(Cliente cliente, ClienteRequest datos) {
        boolean habilitada = datos.tipoCliente().equals("contratista") && datos.cuentaCorrienteHabilitada();
        if (!habilitada && cliente.getSaldoAdeudado() > 0)
            throw new ReglaNegocioException("No se puede quitar la cuenta corriente a un cliente con deuda pendiente");
        cliente.setTipoCliente(datos.tipoCliente());
        cliente.setCuentaCorrienteHabilitada(habilitada);
    }
}
