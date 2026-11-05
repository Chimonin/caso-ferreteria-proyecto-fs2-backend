package fullstackeando.aura.pagos.exception;

//se lanza cuando no se cumple una regla de negocio
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
