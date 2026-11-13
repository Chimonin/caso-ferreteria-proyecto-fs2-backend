package fullstackeando.aura.productos;

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
class ProductoTest {

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

    private Map<String, Object> datosProducto() {
        return new LinkedHashMap<>(Map.of("codigo", "T" + UUID.randomUUID().toString().replace("-", "").substring(0, 9).toUpperCase(),
                "nombre", "Producto test", "categoria", "Pruebas", "subcategoria", "Pruebas",
                "marca", "Test", "unidad", "Unidad", "precio", 1000, "stock", 10, "stockMinimo", 3));
    }

    private String crearProducto(Map<String, Object> datos) {
        Respuesta r = llamar(HttpMethod.POST, "/api/productos", tokenAdmin, datos);
        assertEquals(201, r.status(), "Respuesta: " + r.cuerpo());
        return (String) r.json().get("codigo");
    }

    private void darDeBaja(String codigo) {
        assertEquals(204, llamar(HttpMethod.DELETE, "/api/productos/" + codigo, tokenAdmin, null).status());
    }

    @Test
    void catalogoYDetalleSonPublicos() {
        Respuesta lista = llamar(HttpMethod.GET, "/api/productos", null, null);
        assertEquals(200, lista.status());
        assumeTrue(!lista.lista().isEmpty(), "Se necesita al menos un producto activo");
        var producto = lista.lista().getFirst();
        Respuesta detalle = llamar(HttpMethod.GET, "/api/productos/" + producto.get("codigo"), null, null);
        assertEquals(200, detalle.status());
        assertEquals(producto.get("nombre"), detalle.json().get("nombre"));
        assertNotNull(detalle.json().get("precio")); assertNotNull(detalle.json().get("stock"));
    }

    @Test
    void adminCreaEditaYDaDeBajaUnProducto() {
        Map<String, Object> datos = datosProducto();
        String codigo = crearProducto(datos);
        boolean eliminado = false;
        try {
            datos.put("nombre", "Producto editado");
            Respuesta editado = llamar(HttpMethod.PUT, "/api/productos/" + codigo, tokenAdmin, datos);
            assertEquals(200, editado.status());
            assertEquals("Producto editado", editado.json().get("nombre"));
            Respuesta cambio = llamar(HttpMethod.PATCH, "/api/productos/" + codigo, tokenAdmin, Map.of("precio", 1500, "stock", 2));
            assertEquals(200, cambio.status());
            assertEquals(1500, numero(cambio.json().get("precio")));
            assertEquals(true, cambio.json().get("stockBajo"));
            darDeBaja(codigo); eliminado = true;
            assertEquals(404, llamar(HttpMethod.GET, "/api/productos/" + codigo, null, null).status());
            assertTrue(llamar(HttpMethod.GET, "/api/productos", null, null).lista().stream()
                    .noneMatch(p -> codigo.equals(p.get("codigo"))));
            //el codigo sigue reservado porque la baja conserva el historial
            assertEquals(409, llamar(HttpMethod.POST, "/api/productos", tokenAdmin, datos).status());
        } finally { if (!eliminado) darDeBaja(codigo); }
    }

    @Test
    void vendedorSoloCambiaStockYClienteNoPuedeModificarlo() {
        String codigo = crearProducto(datosProducto());
        try {
            Respuesta cambio = llamar(HttpMethod.PATCH, "/api/productos/" + codigo + "/stock", tokenVendedor, Map.of("stock", 3));
            assertEquals(200, cambio.status());
            assertEquals(3, numero(cambio.json().get("stock")));
            assertEquals(false, cambio.json().get("stockBajo"));
            assertEquals(403, llamar(HttpMethod.PATCH, "/api/productos/" + codigo, tokenVendedor, Map.of("precio", 1)).status());
            assertEquals(400, llamar(HttpMethod.PATCH, "/api/productos/" + codigo + "/stock", tokenVendedor,
                    Map.of("stock", 3, "precio", 1)).status());
            assertEquals(403, llamar(HttpMethod.DELETE, "/api/productos/" + codigo, tokenVendedor, null).status());
            assertEquals(403, llamar(HttpMethod.PATCH, "/api/productos/" + codigo + "/stock", tokenJuan, Map.of("stock", 1)).status());
            assertEquals(1000, numero(llamar(HttpMethod.GET, "/api/productos/" + codigo, null, null).json().get("precio")));
        } finally { darDeBaja(codigo); }
    }

    @Test
    void datosInvalidosYCodigoDuplicadoSeRechazan() {
        Map<String, Object> datos = datosProducto();
        String codigo = crearProducto(datos);
        try {
            assertEquals(409, llamar(HttpMethod.POST, "/api/productos", tokenAdmin, datos).status());
            assertEquals(400, llamar(HttpMethod.POST, "/api/productos", tokenAdmin, Map.of()).status());
            for (Object cantidad : List.of(-1, 1.5)) {
                assertEquals(400, llamar(HttpMethod.PATCH, "/api/productos/" + codigo + "/stock",
                        tokenAdmin, Map.of("stock", cantidad)).status());
            }
            assertEquals(10, numero(llamar(HttpMethod.GET, "/api/productos/" + codigo, null, null).json().get("stock")));
        } finally { darDeBaja(codigo); }
    }

    @Test
    void crearProductoRequiereSesionDeAdministrador() {
        assertEquals(401, llamar(HttpMethod.POST, "/api/productos", null, datosProducto()).status());
        for (String token : List.of(tokenVendedor, tokenJuan))
            assertEquals(403, llamar(HttpMethod.POST, "/api/productos", token, datosProducto()).status());
    }

}
