package Fullstackeando.Aura.usuarios.service;

import Fullstackeando.Aura.usuarios.dto.ClienteResponse;
import Fullstackeando.Aura.usuarios.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<ClienteResponse> listarClientes() {
        return clienteRepository.findByUsuarioActivoTrue().stream()
                .map(ClienteResponse::new)
                .toList();
    }

    public ClienteResponse obtenerCliente(Long id) {
        return clienteRepository.findByIdAndUsuarioActivoTrue(id)
                .map(ClienteResponse::new)
                .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado"));
    }
}
