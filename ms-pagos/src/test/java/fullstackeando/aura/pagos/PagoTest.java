package fullstackeando.aura.pagos;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@EnabledIfEnvironmentVariable(named = "BACKEND_URL", matches = ".+")
class PagoTest {

    private static final String BACKEND = System.getenv("BACKEND_URL");
    private static final RestClient http = RestClient.create();

    private static String tokenAdmin;
    private static String tokenVendedor;
    private static String tokenJuan;
    private static String tokenAna;

    // ---------- ayudas para llamar al backend ----------

    //lo que respondio el backend: el codigo HTTP y el JSON (Map o List)
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
        //exchange: devuelve la respuesta aunque sea 4xx (no lanza excepcion), para poder revisar los errores
        return peticion.exchange((req, res) -> {
            int status = res.getStatusCode().value();
            Object cuerpo = null;
            if (status != 204) {
                try {
                    cuerpo = res.bodyTo(Object.class);
                } catch (RuntimeException sinCuerpo) {
                    cuerpo = null;
                }
            }
            return new Respuesta(status, cuerpo);
        });
    }

    private static String login(String correo) {
        Respuesta r = llamar(HttpMethod.POST, "/api/auth/login", null, Map.of("correo", correo, "clave", "1234"));
        assertEquals(200, r.status(), "No se pudo iniciar sesion con " + correo + ". ¿Esta encendido el backend en " + BACKEND + "?");
        return (String) r.json().get("token");
    }

    private static int numero(Object valor) {
        return ((Number) valor).intValue();
    }

    //lee la deuda de Ana desde ms-usuarios (Ana puede ver su propia cuenta)
    private int deudaAna() {
        Respuesta r = llamar(HttpMethod.GET, "/api/clientes/4", tokenAna, null);
        assertEquals(200, r.status());
        return numero(r.json().get("saldoAdeudado"));
    }

    private List<Map<String, Object>> pagosDeAna() {
        Respuesta r = llamar(HttpMethod.GET, "/api/pagos/cliente/4", tokenAna, null);
        assertEquals(200, r.status());
        return r.lista();
    }

    private Map<String, Object> pago(long clienteId, int monto) {
        return Map.of("clienteId", clienteId, "monto", monto);
    }

    @BeforeAll
    static void iniciarSesion() {
        tokenAdmin = login("admin@duoc.cl");
        tokenVendedor = login("vendedor@duoc.cl");
        tokenJuan = login("juan.perez@gmail.com");
        tokenAna = login("ana.torres@gmail.com");
    }

    // ---------- pago que sale bien ----------

    @Test
    void elPagoDeAnaRestaLaDeudaEnMsUsuariosYQuedaEnElHistorial() {
        int deudaAntes = deudaAna();
        int pagosAntes = pagosDeAna().size();
        assumeTrue(deudaAntes >= 1000, "Ana debe menos de 1000, no se puede probar el pago");

        Respuesta r = llamar(HttpMethod.POST, "/api/pagos", tokenAna, pago(4, 1000));

        assertEquals(201, r.status(), "Respuesta: " + r.cuerpo());
        assertEquals(1000, numero(r.json().get("monto")));
        int id = numero(r.json().get("id"));

        //la deuda bajo de verdad en ms-usuarios y el pago quedo guardado en ms-pagos
        assertEquals(deudaAntes - 1000, deudaAna());
        assertEquals(pagosAntes + 1, pagosDeAna().size());

        //el admin lo ve en AdminCredito
        Respuesta todos = llamar(HttpMethod.GET, "/api/pagos", tokenAdmin, null);
        assertEquals(200, todos.status());
        assertTrue(todos.lista().stream().anyMatch(p -> numero(p.get("id")) == id));
    }


    @Test
    void anaNoPuedePagarMasDeLoQueDebe() {
        int deudaAntes = deudaAna();
        int pagosAntes = pagosDeAna().size();

        Respuesta r = llamar(HttpMethod.POST, "/api/pagos", tokenAna, pago(4, deudaAntes + 1));

        assertEquals(409, r.status());
        assertEquals("El monto no puede ser mayor a la deuda", r.mensaje());
        assertEquals(deudaAntes, deudaAna());
        assertEquals(pagosAntes, pagosDeAna().size());
    }

    @Test
    void juanNoPuedePagarPorqueNoTieneCuentaCorriente() {
        Respuesta r = llamar(HttpMethod.POST, "/api/pagos", tokenJuan, pago(3, 1000));

        assertEquals(409, r.status());
        assertEquals("El cliente no tiene cuenta corriente habilitada", r.mensaje());
    }

    @Test
    void juanNoPuedePagarLaDeudaDeAna() {
        int deudaAntes = deudaAna();

        Respuesta r = llamar(HttpMethod.POST, "/api/pagos", tokenJuan, pago(4, 1000));

        assertEquals(403, r.status());
        assertEquals(deudaAntes, deudaAna());
    }

    @Test
    void pagarMontoCeroSeRechaza() {
        int deudaAntes = deudaAna();

        Respuesta r = llamar(HttpMethod.POST, "/api/pagos", tokenAna, pago(4, 0));

        assertEquals(400, r.status());
        assertEquals("El monto debe ser un número entero mayor a 0", r.mensaje());
        assertEquals(deudaAntes, deudaAna());
    }


    @Test
    void elVendedorNoVeLosPagos() {
        assertEquals(403, llamar(HttpMethod.GET, "/api/pagos", tokenVendedor, null).status());
    }

    @Test
    void sinIniciarSesionNoSePuedePagar() {
        assertEquals(401, llamar(HttpMethod.POST, "/api/pagos", null, pago(4, 1000)).status());
    }
}
