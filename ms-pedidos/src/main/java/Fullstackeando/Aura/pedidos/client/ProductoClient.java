package Fullstackeando.Aura.pedidos.client;

import Fullstackeando.Aura.pedidos.dto.ItemCarritoRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

//le habla por HTTP a ms-productos
@Component
public class ProductoClient {

    private final RestClient restClient;

    public ProductoClient(@Value("${servicios.productos.url}") String url) {
        this.restClient = RestClient.builder().baseUrl(url).build();
    }

    //POST /internal/productos/descontar-stock
    //ms-productos revisa que todos existan y tengan stock, y descuenta todo junto
    //si uno falla, no descuenta ninguno y responde 409 con el mensaje
    public List<ProductoDescontado> descontarStock(List<ItemCarritoRequest> items) {
        try {
            return restClient.post()
                    .uri("/internal/productos/descontar-stock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(items)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ProductoDescontado>>() {});
        } catch (RestClientException e) {
            throw ErrorRemoto.traducir(e, "productos");
        }
    }

    //POST /internal/productos/reponer-stock
    //devuelve el stock si el pedido fallo despues de descontarlo, a modo de compensacion
    public void reponerStock(List<ItemCarritoRequest> items) {
        try {
            restClient.post()
                    .uri("/internal/productos/reponer-stock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(items)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw ErrorRemoto.traducir(e, "productos");
        }
    }
}
