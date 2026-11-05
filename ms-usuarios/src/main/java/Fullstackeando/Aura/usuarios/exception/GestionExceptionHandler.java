package Fullstackeando.Aura.usuarios.exception;

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

    @ExceptionHandler({HttpMessageNotReadableException.class, HandlerMethodValidationException.class,
            ConstraintViolationException.class})
    public ResponseEntity<Map<String, String>> validacion(Exception error) {
        return ResponseEntity.badRequest().body(Map.of("error", "VALIDACION", "mensaje", "Revisa los campos obligatorios y usa cantidades enteras válidas"));
    }
}
