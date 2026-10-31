package Fullstackeando.Aura.pedidos.exception;

//se lanza cuando otro microservicio no responde (esta apagado o fallo)
public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
