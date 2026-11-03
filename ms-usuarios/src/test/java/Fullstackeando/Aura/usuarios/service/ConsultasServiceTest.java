package Fullstackeando.Aura.usuarios.service;

import Fullstackeando.Aura.usuarios.model.Cliente;
import Fullstackeando.Aura.usuarios.model.Usuario;
import Fullstackeando.Aura.usuarios.repository.ClienteRepository;
import Fullstackeando.Aura.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultasServiceTest {

    @Test
    void listarUsuariosDevuelveElNombreCompletoYElRol() {
        var repository = mock(UsuarioRepository.class);
        var usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Carla");
        usuario.setApellidos("Rojas");
        usuario.setRol("admin");
        when(repository.findByActivoTrue()).thenReturn(List.of(usuario));

        var resultado = new UsuarioService(repository).listarUsuarios();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.getFirst().getId());
        assertEquals("Carla Rojas", resultado.getFirst().getNombreCompleto());
        assertEquals("admin", resultado.getFirst().getRol());
    }

    @Test
    void obtenerUsuarioInexistenteMuestraError() {
        var repository = mock(UsuarioRepository.class);
        when(repository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        var error = assertThrows(NoSuchElementException.class,
                () -> new UsuarioService(repository).obtenerUsuario(99L));

        assertEquals("Usuario no encontrado", error.getMessage());
    }

    @Test
    void listarClientesUneDatosDelUsuarioYCuentaCorriente() {
        var repository = mock(ClienteRepository.class);
        var usuario = new Usuario();
        usuario.setId(4L);
        usuario.setNombre("Ana");
        usuario.setApellidos("Torres");
        usuario.setCorreo("ana@gmail.com");
        usuario.setDireccion("Av. del Mar 3200");
        var cliente = new Cliente();
        cliente.setId(4L);
        cliente.setUsuario(usuario);
        cliente.setTipoCliente("contratista");
        cliente.setCuentaCorrienteHabilitada(true);
        cliente.setSaldoAdeudado(32000);
        when(repository.findByUsuarioActivoTrue()).thenReturn(List.of(cliente));

        var resultado = new ClienteService(repository).listarClientes();

        assertEquals(1, resultado.size());
        var respuesta = resultado.getFirst();
        assertEquals(4L, respuesta.getId());
        assertEquals("Ana Torres", respuesta.getNombreCompleto());
        assertEquals("ana@gmail.com", respuesta.getCorreo());
        assertEquals("Av. del Mar 3200", respuesta.getDireccion());
        assertEquals("contratista", respuesta.getTipoCliente());
        assertTrue(respuesta.isCuentaCorrienteHabilitada());
        assertEquals(32000, respuesta.getSaldoAdeudado());
    }

    @Test
    void obtenerClienteInexistenteMuestraError() {
        var repository = mock(ClienteRepository.class);
        when(repository.findByIdAndUsuarioActivoTrue(99L)).thenReturn(Optional.empty());

        var error = assertThrows(NoSuchElementException.class,
                () -> new ClienteService(repository).obtenerCliente(99L));

        assertEquals("Cliente no encontrado", error.getMessage());
    }
}
