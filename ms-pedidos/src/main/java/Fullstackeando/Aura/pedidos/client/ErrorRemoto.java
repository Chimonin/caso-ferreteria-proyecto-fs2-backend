package Fullstackeando.Aura.pedidos.client;

import Fullstackeando.Aura.pedidos.exception.ReglaNegocioException;
import Fullstackeando.Aura.pedidos.exception.ServicioNoDisponibleException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;
import java.util.NoSuchElementException;

//traduce los errores que responde otro microservicio a nuestras propias excepciones
//asi el GlobalExceptionHandler los responde igual que si hubieran pasado aqui
public final class ErrorRemoto {

    private ErrorRemoto() {
    }

    public static RuntimeException traducir(RestClientException e, String servicio) {
        //el otro servicio respondio, pero con un error (404, 409, 400...)
        if (e instanceof RestClientResponseException respuesta) {
            String mensaje = leerMensaje(respuesta, servicio);
            int codigo = respuesta.getStatusCode().value();

            if (codigo == 404) return new NoSuchElementException(mensaje);
            if (codigo == 400 || codigo == 409) return new ReglaNegocioException(mensaje);
        }

        //el otro servicio no respondio (esta apagado) o fallo por dentro (500)
        return new ServicioNoDisponibleException(
                "El servicio de " + servicio + " no está disponible. Intenta más tarde.");
    }

    //lee el "mensaje" del json de error que manda el GlobalExceptionHandler del otro servicio
    private static String leerMensaje(RestClientResponseException respuesta, String servicio) {
        try {
            Map<?, ?> body = respuesta.getResponseBodyAs(Map.class);
            if (body != null && body.get("mensaje") != null) {
                return body.get("mensaje").toString();
            }
        } catch (RuntimeException ignorado) {
            //si el cuerpo no es json, se usa el mensaje generico de abajo
        }
        return "Error en el servicio de " + servicio + ".";
    }
}
