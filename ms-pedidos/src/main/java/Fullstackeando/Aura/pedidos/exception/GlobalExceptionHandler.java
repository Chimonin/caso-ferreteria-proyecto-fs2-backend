//traduce los errores en respuestas http
package Fullstackeando.Aura.pedidos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //errores de @Valid (Bean Validation) 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    //MethodArgumentNotValidException ex es la excepcion parametro que recibe el metodo

    //response entity representa una respuesta http completa con body contenido y cabeceras

    //map string object es el tipo del body
    //un diccionario con strings como llaves y objects como valores
    public ResponseEntity<Map<String, Object>> manejarValidacion(MethodArgumentNotValidException ex) {
        //crea un mapa vacio donde se va guardando nombre del campo y mensaje de error
        Map<String, String> campos = new HashMap<>();
        
        //getbindingresults trae todos los detalles de que fallo en la validacion

        //getfielderrors extrae la lista de errores que estan asociados a un campo particular de bean validation

        //for each error ejecuta el codigo de adentro
        //ambos van agregando entradas a campos
        ex.getBindingResult().getFieldErrors().forEach(error ->
                //agrega una entrada a un map
                //getfield da el nombre del campo que fallo
                //getdefaultmessage obtiene el mensaje de descripcion del error
                campos.put(error.getField(), error.getDefaultMessage())
        );

        //error global
        //aqui caen las validaciones especiales de bean validation
        ex.getBindingResult().getGlobalErrors().forEach(error ->
                campos.put(error.getObjectName(), error.getDefaultMessage())
        );

        Map<String, Object> body = new HashMap<>();
        body.put("error", "VALIDACION");
        //"mensaje" trae el primer error, para mostrarlo directo en el frontend
        body.put("mensaje", campos.values().stream().findFirst().orElse("Datos no válidos"));
        body.put("campos", campos);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    //regla de negocio no cumplida 409
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, Object>> manejarReglaNegocio(ReglaNegocioException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "REGLA_NEGOCIO");
        body.put("mensaje", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    //recurso no encontrado 404
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(NoSuchElementException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "NO_ENCONTRADO");
        body.put("mensaje", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    //otro microservicio no respondio 503
    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> manejarServicioNoDisponible(ServicioNoDisponibleException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "SERVICIO_NO_DISPONIBLE");
        body.put("mensaje", ex.getMessage());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }
}
