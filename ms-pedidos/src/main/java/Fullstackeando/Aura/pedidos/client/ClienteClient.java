package Fullstackeando.Aura.pedidos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

//le habla por HTTP a ms-usuarios
@Component
public class ClienteClient {

    private final RestClient restClient;

    public ClienteClient(@Value("${servicios.usuarios.url}") String url) {
        this.restClient = RestClient.builder().baseUrl(url).build();
    }

    //GET /internal/clientes/3
    //si no existe, ms-usuarios responde 404 y aqui se lanza NoSuchElementException
    public ClienteRemoto obtenerCliente(Long clienteId) {
        try {
            return restClient.get()
                    .uri("/internal/clientes/{id}", clienteId)
                    .retrieve()
                    .body(ClienteRemoto.class);
        } catch (RestClientException e) {
            throw ErrorRemoto.traducir(e, "usuarios");
        }
    }

    //PATCH /internal/clientes/4/sumar-deuda   { "monto": 38750 }
    //la compra a credito se suma a la deuda
    public void sumarDeuda(Long clienteId, int monto) {
        cambiarDeuda(clienteId, "sumar-deuda", monto);
    }

    //PATCH /internal/clientes/4/restar-deuda   { "monto": 38750 }
    //se usa para deshacer la deuda si el pedido fallo (compensacion)
    public void restarDeuda(Long clienteId, int monto) {
        cambiarDeuda(clienteId, "restar-deuda", monto);
    }

    private void cambiarDeuda(Long clienteId, String accion, int monto) {
        try {
            restClient.patch()
                    .uri("/internal/clientes/{id}/{accion}", clienteId, accion)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("monto", monto))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw ErrorRemoto.traducir(e, "usuarios");
        }
    }
}
