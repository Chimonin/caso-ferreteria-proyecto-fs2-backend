package fullstackeando.aura.usuarios;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@EnabledIfEnvironmentVariable(named = "BACKEND_URL", matches = ".+")
class ClienteTest {

    private static final String BACKEND = System.getenv("BACKEND_URL");
    private static final RestClient http = RestClient.create();
    private static String tokenAdmin;
    private static String tokenVendedor;
    private static String tokenJuan;

    //lo que respondio el backend: codigo HTTP y JSON
    record Respuesta(int status, Object cuerpo) {
        @SuppressWarnings("unchecked")
        Map<String, Object> json() { return (Map<String, Object>) cuerpo; }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lista() { return (List<Map<String, Object>>) cuerpo; }
        String mensaje() { return (String) json().get("mensaje"); }
    }

    private static Respuesta llamar(HttpMethod metodo, String ruta, String token, Object body) {
        RestClient.RequestBodySpec peticion = http.method(metodo).uri(BACKEND + ruta);
        if (token != null) peticion = peticion.header("Authorization", "Bearer " + token);
        if (body != null) peticion = peticion.contentType(MediaType.APPLICATION_JSON).body(body);
        //exchange permite revisar tambien las respuestas de error
        return peticion.exchange((req, res) -> {
            Object cuerpo = null;
            if (res.getStatusCode().value() != 204) {
                try { cuerpo = res.bodyTo(Object.class); }
                catch (RuntimeException sinCuerpo) { cuerpo = null; }
            }
            return new Respuesta(res.getStatusCode().value(), cuerpo);
        });
    }

    private static String login(String correo) {
        Respuesta r = llamar(HttpMethod.POST, "/api/auth/login", null, Map.of("correo", correo, "clave", "1234"));
        assertEquals(200, r.status(), "No se pudo iniciar sesion con " + correo + ". Revisa el backend en " + BACKEND);
        return (String) r.json().get("token");
    }

    private static int numero(Object valor) { return ((Number) valor).intValue(); }

    @BeforeAll
    static void iniciarSesion() {
        tokenAdmin = login("admin@duoc.cl");
        tokenVendedor = login("vendedor@duoc.cl");
        tokenJuan = login("juan.perez@gmail.com");
    }

    //cada prueba crea sus propios datos para no depender del orden de ejecucion
    private Map<String, Object> datosUsuario() {
        Map<String, Object> datos = new LinkedHashMap<>();
        String unico = UUID.randomUUID().toString().replace("-", "");
        String rut = String.valueOf(java.util.concurrent.ThreadLocalRandom.current().nextInt(30000000, 99999999)) + "K";
        datos.put("rut", rut); datos.put("nombre", "Prueba"); datos.put("apellidos", "Testing");
        datos.put("correo", "test." + unico + "@gmail.com"); datos.put("clave", "1234");
        datos.put("region", "Coquimbo"); datos.put("comuna", "La Serena"); datos.put("direccion", "Calle Prueba 123");
        datos.put("tipoCliente", "contratista");
        return datos;
    }

    private long crearUsuario(Map<String, Object> datos) {
        Respuesta r = llamar(HttpMethod.POST, "/api/usuarios", tokenAdmin, datos);
        assertEquals(201, r.status(), "Respuesta: " + r.cuerpo());
        return ((Number) r.json().get("id")).longValue();
    }

    private void darDeBaja(long id) {
        assertEquals(204, llamar(HttpMethod.DELETE, "/api/usuarios/" + id, tokenAdmin, null).status());
    }

    @Test
    void clienteVeSuCuentaPeroNoLaDeOtroNiElListadoGeneral() {
        Respuesta cuenta = llamar(HttpMethod.GET, "/api/clientes/3", tokenJuan, null);
        assertEquals(200, cuenta.status());
        assertEquals(3, numero(cuenta.json().get("id")));
        assertFalse(cuenta.json().containsKey("clave"));
        assertEquals(403, llamar(HttpMethod.GET, "/api/clientes/4", tokenJuan, null).status());
        assertEquals(403, llamar(HttpMethod.GET, "/api/clientes", tokenJuan, null).status());
        assertEquals(403, llamar(HttpMethod.GET, "/api/clientes", tokenVendedor, null).status());
        assertEquals(200, llamar(HttpMethod.GET, "/api/clientes", tokenAdmin, null).status());
    }

    @Test
    void adminEditaTipoDeClienteYDaDeBajaSinDeuda() {
        Map<String, Object> datos = datosUsuario(); datos.put("rol", "cliente");
        datos.put("cuentaCorrienteHabilitada", true);
        long id = crearUsuario(datos);
        boolean eliminado = false;
        try {
            String token = login((String) datos.get("correo"));
            Respuesta original = llamar(HttpMethod.GET, "/api/clientes/" + id, token, null);
            assertEquals(200, original.status());
            assertEquals(true, original.json().get("cuentaCorrienteHabilitada"));
            Respuesta r = llamar(HttpMethod.PUT, "/api/clientes/" + id, tokenAdmin,
                    Map.of("tipoCliente", "particular", "cuentaCorrienteHabilitada", true));
            assertEquals(200, r.status());
            assertEquals(false, r.json().get("cuentaCorrienteHabilitada"));
            assertEquals(403, llamar(HttpMethod.PUT, "/api/clientes/" + id, token,
                    Map.of("tipoCliente", "contratista", "cuentaCorrienteHabilitada", true)).status());
            assertEquals(204, llamar(HttpMethod.DELETE, "/api/clientes/" + id, tokenAdmin, null).status());
            eliminado = true;
            assertEquals(404, llamar(HttpMethod.GET, "/api/clientes/" + id, tokenAdmin, null).status());
        } finally { if (!eliminado) darDeBaja(id); }
    }

    @Test
    void noSePuedeDuplicarClienteNiUsarUnTipoInvalido() {
        Map<String, Object> datos = datosUsuario(); datos.put("rol", "cliente");
        long id = crearUsuario(datos);
        try {
            assertEquals(409, llamar(HttpMethod.POST, "/api/clientes", tokenAdmin,
                    Map.of("id", id, "tipoCliente", "particular", "cuentaCorrienteHabilitada", false)).status());
            assertEquals(400, llamar(HttpMethod.PUT, "/api/clientes/" + id, tokenAdmin,
                    Map.of("tipoCliente", "empresa", "cuentaCorrienteHabilitada", false)).status());
        } finally { darDeBaja(id); }
    }

    @Test
    void deudaImpideDarDeBajaOCambiarLaCuentaCorriente() {
        Map<String, Object> datos = datosUsuario(); datos.put("rol", "cliente");
        datos.put("cuentaCorrienteHabilitada", true);
        long id = crearUsuario(datos);
        String codigo = "T" + UUID.randomUUID().toString().replace("-", "").substring(0, 9).toUpperCase();
        int deudaPendiente = 0;
        try {
            String token = login((String) datos.get("correo"));
            Respuesta producto = llamar(HttpMethod.POST, "/api/productos", tokenAdmin,
                    Map.of("codigo", codigo, "nombre", "Producto test cliente", "categoria", "Pruebas",
                            "subcategoria", "Pruebas", "marca", "Test", "unidad", "Unidad",
                            "precio", 100, "stock", 2, "stockMinimo", 1));
            assertEquals(201, producto.status());
            Respuesta pedido = llamar(HttpMethod.POST, "/api/pedidos", token,
                    Map.of("clienteId", id, "medioPago", "cuenta_corriente", "entrega", Map.of("modalidad", "retiro"),
                            "items", List.of(Map.of("codigo", codigo, "cantidad", 1))));
            assertEquals(201, pedido.status(), "Respuesta: " + pedido.cuerpo());
            deudaPendiente = numero(pedido.json().get("total"));
            Respuesta cuenta = llamar(HttpMethod.GET, "/api/clientes/" + id, token, null);
            assertEquals(200, cuenta.status());
            assertEquals(deudaPendiente, numero(cuenta.json().get("saldoAdeudado")));
            assertEquals(409, llamar(HttpMethod.DELETE, "/api/clientes/" + id, tokenAdmin, null).status());
            assertEquals(409, llamar(HttpMethod.DELETE, "/api/usuarios/" + id, tokenAdmin, null).status());
            assertEquals(409, llamar(HttpMethod.PUT, "/api/clientes/" + id, tokenAdmin,
                    Map.of("tipoCliente", "particular", "cuentaCorrienteHabilitada", false)).status());
            Respuesta pago = llamar(HttpMethod.POST, "/api/pagos", token, Map.of("clienteId", id, "monto", deudaPendiente));
            assertEquals(201, pago.status());
            deudaPendiente = 0;
            assertEquals(0, numero(llamar(HttpMethod.GET, "/api/clientes/" + id, token, null).json().get("saldoAdeudado")));
        } finally {
            //si fallo despues de comprar, se cancela la deuda antes de desactivar el cliente
            if (deudaPendiente > 0) {
                String token = login((String) datos.get("correo"));
                assertEquals(201, llamar(HttpMethod.POST, "/api/pagos", token,
                        Map.of("clienteId", id, "monto", deudaPendiente)).status());
            }
            Respuesta baja = llamar(HttpMethod.DELETE, "/api/productos/" + codigo, tokenAdmin, null);
            assertTrue(baja.status() == 204 || baja.status() == 404);
            darDeBaja(id);
        }
    }

}
