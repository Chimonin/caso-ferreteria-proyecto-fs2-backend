package fullstackeando.aura.usuarios.service;

import fullstackeando.aura.usuarios.dto.*;
import fullstackeando.aura.usuarios.model.*;
import fullstackeando.aura.usuarios.repository.*;
import fullstackeando.aura.usuarios.exception.ReglaNegocioException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.*;

@Service
@Transactional
public class UsuarioGestionService {
    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final PasswordEncoder encoder;
    private final Map<String, List<String>> regiones;

    public UsuarioGestionService(UsuarioRepository usuarios, ClienteRepository clientes, PasswordEncoder encoder,
                                 ObjectMapper mapper) throws IOException {
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.encoder = encoder;
        try (var archivo = new ClassPathResource("regionesComunas.json").getInputStream()) {
            regiones = mapper.readValue(archivo, new TypeReference<Map<String, List<String>>>() {});
        }
    }

    public UsuarioResponse crearUsuario(UsuarioRequest datos) {
        usuarios.bloquearUsuarios();
        validar(datos, null);
        if (datos.clave() == null || datos.clave().length() < 4 || datos.clave().length() > 10)
            throw new ReglaNegocioException("La contraseña debe tener entre 4 y 10 caracteres");
        Usuario usuario = new Usuario();
        copiarDatos(usuario, datos);
        usuario.setClave(encoder.encode(datos.clave()));
        usuarios.saveAndFlush(usuario);
        sincronizarCliente(usuario, datos);
        return new UsuarioResponse(usuario);
    }

    public UsuarioResponse actualizarUsuario(Long id, UsuarioRequest datos) {
        var todos = usuarios.bloquearUsuarios();
        Usuario usuario = buscarActivo(todos, id);
        validar(datos, id);
        if (usuario.getRol().equals("admin") && !datos.rol().equals("admin")) exigirOtroAdmin(todos);
        //la edicion de datos no modifica la contraseña
        sincronizarCliente(usuario, datos);
        copiarDatos(usuario, datos);
        return new UsuarioResponse(usuario);
    }

    public void eliminarUsuario(Long id) {
        var todos = usuarios.bloquearUsuarios();
        Usuario usuario = buscarActivo(todos, id);
        if (usuario.getRol().equals("admin")) exigirOtroAdmin(todos);
        clientes.findById(id).ifPresent(cliente -> {
            if (cliente.getSaldoAdeudado() > 0)
                throw new ReglaNegocioException("El cliente tiene deuda pendiente en su cuenta corriente");
        });
        //baja logica para conservar la identidad de los pedidos historicos
        usuario.setActivo(false);
    }

    private Usuario buscarActivo(List<Usuario> todos, Long id) {
        return todos.stream().filter(u -> u.getId().equals(id) && u.isActivo()).findFirst()
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }

    private void exigirOtroAdmin(List<Usuario> todos) {
        if (todos.stream().filter(u -> u.isActivo() && u.getRol().equals("admin")).count() <= 1)
            throw new ReglaNegocioException("Debe quedar al menos un administrador");
    }

    private void validar(UsuarioRequest datos, Long id) {
        String correo = datos.correo().trim().toLowerCase(Locale.ROOT);
        String dominio = correo.substring(correo.lastIndexOf('@') + 1);
        if (!Set.of("duoc.cl", "profesor.duoc.cl", "gmail.com").contains(dominio))
            throw new ReglaNegocioException("El correo debe ser @duoc.cl, @profesor.duoc.cl o @gmail.com");
        if (!regiones.getOrDefault(datos.region(), List.of()).contains(datos.comuna()))
            throw new ReglaNegocioException("La comuna no pertenece a la región elegida");
        usuarios.findByCorreoIgnoreCase(correo).filter(u -> !u.getId().equals(id)).ifPresent(u -> {
            throw new ReglaNegocioException("Ya existe un usuario con ese correo");
        });
        usuarios.findByRutIgnoreCase(datos.rut().trim()).filter(u -> !u.getId().equals(id)).ifPresent(u -> {
            throw new ReglaNegocioException("Ya existe un usuario con ese RUT");
        });
        if (datos.rol().equals("cliente") && !Set.of("particular", "contratista").contains(
                datos.tipoCliente() == null ? "" : datos.tipoCliente()))
            throw new ReglaNegocioException("Selecciona si el cliente es particular o contratista");
    }

    private void sincronizarCliente(Usuario usuario, UsuarioRequest datos) {
        var anterior = clientes.findById(usuario.getId());
        if (!datos.rol().equals("cliente")) {
            anterior.ifPresent(cliente -> {
                if (cliente.getSaldoAdeudado() > 0)
                    throw new ReglaNegocioException("El cliente tiene deuda pendiente en su cuenta corriente");
                clientes.delete(cliente);
            });
            return;
        }
        boolean habilitada = datos.tipoCliente().equals("contratista") && Boolean.TRUE.equals(datos.cuentaCorrienteHabilitada());
        Cliente cliente = anterior.orElseGet(Cliente::new);
        if (!habilitada && cliente.getSaldoAdeudado() > 0)
            throw new ReglaNegocioException("No se puede quitar la cuenta corriente a un cliente con deuda pendiente");
        cliente.setUsuario(usuario);
        cliente.setTipoCliente(datos.tipoCliente());
        cliente.setCuentaCorrienteHabilitada(habilitada);
        if (anterior.isEmpty()) clientes.save(cliente);
    }

    private void copiarDatos(Usuario usuario, UsuarioRequest datos) {
        usuario.setRut(datos.rut().trim().toUpperCase(Locale.ROOT));
        usuario.setNombre(datos.nombre().trim());
        usuario.setApellidos(datos.apellidos().trim());
        usuario.setCorreo(datos.correo().trim().toLowerCase(Locale.ROOT));
        usuario.setRol(datos.rol());
        usuario.setRegion(datos.region());
        usuario.setComuna(datos.comuna());
        usuario.setDireccion(datos.direccion().trim());
    }
}
