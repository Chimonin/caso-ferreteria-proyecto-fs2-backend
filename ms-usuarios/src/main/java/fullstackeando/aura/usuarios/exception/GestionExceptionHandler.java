package fullstackeando.aura.usuarios.exception;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;

@RestControllerAdvice
public class GestionExceptionHandler {
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, String>> negocio(ReglaNegocioException error) {
        return ResponseEntity.status(409).body(Map.of("error", "REGLA_NEGOCIO", "mensaje", error.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> duplicado(DataIntegrityViolationException error) {
        return ResponseEntity.status(409).body(Map.of("error", "CONFLICTO", "mensaje", "Los datos ya existen o no cumplen las restricciones"));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, String>> validacionLista(HandlerMethodValidationException error) {
        String mensaje = error.getAllErrors().stream()
                .map(e -> e.getDefaultMessage()).findFirst().orElse("Datos no válidos");
        return validacion(mensaje);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> validacionParametros(ConstraintViolationException error) {
        String mensaje = error.getConstraintViolations().stream()
                .map(v -> v.getMessage()).findFirst().orElse("Datos no válidos");
        return validacion(mensaje);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonInvalido(HttpMessageNotReadableException error) {
        return validacion("El formato de los datos no es válido: revisa los nombres de los campos y usa números enteros");
    }

    private ResponseEntity<Map<String, String>> validacion(String mensaje) {
        return ResponseEntity.badRequest().body(Map.of("error", "VALIDACION", "mensaje", mensaje));
    }
}
