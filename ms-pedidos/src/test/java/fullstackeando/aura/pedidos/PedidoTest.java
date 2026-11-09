package fullstackeando.aura.pedidos;

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
class PedidoTest {

    private static final String BACKEND = System.getenv("BACKEND_URL");
    private static final RestClient http = RestClient.create();

    //tokens de los usuarios de los mocks (todos con clave 1234)
    private static String tokenJuan;
    private static String tokenAna;
    private static String tokenVendedor;

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

    //lee el producto desde ms-productos
    private Map<String, Object> producto(String codigo) {
        Respuesta r = llamar(HttpMethod.GET, "/api/productos/" + codigo, null, null);
        assertEquals(200, r.status());
        return r.json();
    }

    private int stock(String codigo) {
        return numero(producto(codigo).get("stock"));
    }

    //lee la deuda de Ana desde ms-usuarios (Ana puede ver su propia cuenta)
    private int deudaAna() {
        Respuesta r = llamar(HttpMethod.GET, "/api/clientes/4", tokenAna, null);
        assertEquals(200, r.status());
        return numero(r.json().get("saldoAdeudado"));
    }

    private List<Map<String, Object>> pedidosDe(long clienteId, String token) {
        Respuesta r = llamar(HttpMethod.GET, "/api/pedidos/cliente/" + clienteId, token, null);
        assertEquals(200, r.status());
        return r.lista();
    }

    //arma el body de un pedido con un solo producto
    private Map<String, Object> pedido(long clienteId, String medioPago, String codigo, int cantidad, boolean despacho) {
        Map<String, Object> entrega = despacho
                ? Map.of("modalidad", "despacho", "comuna", "Coquimbo", "direccion", "Pasaje Los Aromos 45")
                : Map.of("modalidad", "retiro");
        return Map.of(
                "clienteId", clienteId,
                "medioPago", medioPago,
                "entrega", entrega,
                "items", List.of(Map.of("codigo", codigo, "cantidad", cantidad)));
    }

    @BeforeAll
    static void iniciarSesion() {
        tokenJuan = login("juan.perez@gmail.com");
        tokenAna = login("ana.torres@gmail.com");
        tokenVendedor = login("vendedor@duoc.cl");
    }

    // ---------- compras que salen bien ----------

    @Test
    @SuppressWarnings("unchecked")
    void compraAlContadoDeJuanDescuentaStockEnMsProductos() {
        int precio = numero(producto("MC001").get("precio"));
        int stockAntes = stock("MC001");
        int pedidosAntes = pedidosDe(3, tokenJuan).size();
        assumeTrue(stockAntes >= 1, "MC001 no tiene stock para probar");

        Respuesta r = llamar(HttpMethod.POST, "/api/pedidos", tokenJuan, pedido(3, "contado", "MC001", 1, false));

        assertEquals(201, r.status(), "Respuesta: " + r.cuerpo());
        assertEquals("Pendiente", r.json().get("estado"));
        assertEquals(precio, numero(r.json().get("total")));
        Map<String, Object> item = ((List<Map<String, Object>>) r.json().get("items")).getFirst();
        assertEquals(producto("MC001").get("nombre"), item.get("nombre"));

        //el stock bajo de verdad en ms-productos y el pedido quedo guardado
        assertEquals(stockAntes - 1, stock("MC001"));
        assertEquals(pedidosAntes + 1, pedidosDe(3, tokenJuan).size());
    }

    @Test
    void compraACreditoDeAnaSumaLaDeudaEnMsUsuarios() {
        int precio = numero(producto("MC005").get("precio"));
        int stockAntes = stock("MC005");
        int deudaAntes = deudaAna();
        assumeTrue(stockAntes >= 1, "MC005 no tiene stock para probar");

        Respuesta r = llamar(HttpMethod.POST, "/api/pedidos", tokenAna, pedido(4, "cuenta_corriente", "MC005", 1, true));

        assertEquals(201, r.status(), "Respuesta: " + r.cuerpo());
        
        int total = precio + 3990;
        assertEquals(total, numero(r.json().get("total")));

        //la deuda subio de verdad en ms-usuarios y el stock bajo en ms-productos
        assertEquals(deudaAntes + total, deudaAna());
        assertEquals(stockAntes - 1, stock("MC005"));
    }

    @Test
    void elVendedorCambiaElEstadoYJuanLoVe() {
        assumeTrue(stock("MC005") >= 1, "MC005 no tiene stock para probar");
        Respuesta creado = llamar(HttpMethod.POST, "/api/pedidos", tokenJuan, pedido(3, "contado", "MC005", 1, false));
        assertEquals(201, creado.status(), "Respuesta: " + creado.cuerpo());
        int id = numero(creado.json().get("id"));

        Respuesta cambio = llamar(HttpMethod.PATCH, "/api/pedidos/" + id + "/estado", tokenVendedor,
                Map.of("estado", "En preparación"));
        assertEquals(200, cambio.status());

        //Juan ve el estado nuevo en "Mis pedidos"
        Map<String, Object> suPedido = pedidosDe(3, tokenJuan).stream()
                .filter(p -> numero(p.get("id")) == id)
                .findFirst().orElseThrow();
        assertEquals("En preparación", suPedido.get("estado"));
    }

    // ---------- compras que se rechazan: no debe cambiar nada en ningun servicio ----------

    @Test
    void sinStockSuficienteNoSeDescuentaNadaNiSeGuardaElPedido() {
        int stockAntes = stock("MC001");
        int pedidosAntes = pedidosDe(3, tokenJuan).size();

        Respuesta r = llamar(HttpMethod.POST, "/api/pedidos", tokenJuan, pedido(3, "contado", "MC001", stockAntes + 1, false));

        assertEquals(409, r.status());
        assertTrue(r.mensaje().startsWith("No hay stock suficiente"), r.mensaje());
        assertEquals(stockAntes, stock("MC001"));
        assertEquals(pedidosAntes, pedidosDe(3, tokenJuan).size());
    }

    @Test
    void juanNoPuedeComprarACreditoPorqueNoTieneCuentaCorriente() {
        int stockAntes = stock("MC001");

        Respuesta r = llamar(HttpMethod.POST, "/api/pedidos", tokenJuan, pedido(3, "cuenta_corriente", "MC001", 1, false));

        assertEquals(409, r.status());
        assertEquals("La cuenta corriente no está habilitada.", r.mensaje());
        //se rechaza antes de llamar a ms-productos: el stock no cambia
        assertEquals(stockAntes, stock("MC001"));
    }

    @Test
    void comprarUnProductoQueNoExisteSeRechaza() {
        Respuesta r = llamar(HttpMethod.POST, "/api/pedidos", tokenJuan, pedido(3, "contado", "XX999", 1, false));

        assertEquals(409, r.status());
        assertEquals("Uno de los productos ya no existe.", r.mensaje());
    }

    @Test
    void juanNoPuedeComprarANombreDeAna() {
        int deudaAntes = deudaAna();
        int stockAntes = stock("MC005");

        Respuesta r = llamar(HttpMethod.POST, "/api/pedidos", tokenJuan, pedido(4, "cuenta_corriente", "MC005", 1, false));

        assertEquals(403, r.status());
        assertEquals(deudaAntes, deudaAna());
        assertEquals(stockAntes, stock("MC005"));
    }

    @Test
    void sinIniciarSesionNoSePuedeComprar() {
        Respuesta r = llamar(HttpMethod.POST, "/api/pedidos", null, pedido(3, "contado", "MC001", 1, false));

        assertEquals(401, r.status());
    }
}
