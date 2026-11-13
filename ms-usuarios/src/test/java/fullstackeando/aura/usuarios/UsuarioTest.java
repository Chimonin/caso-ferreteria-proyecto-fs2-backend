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
class UsuarioTest {

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
    void registroCreaClienteYPermiteIniciarSesion() {
        Map<String, Object> datos = datosUsuario();
        Respuesta r = llamar(HttpMethod.POST, "/api/auth/registro", null, datos);
        assertEquals(201, r.status(), "Respuesta: " + r.cuerpo());
        @SuppressWarnings("unchecked")
        Map<String, Object> usuario = (Map<String, Object>) r.json().get("usuario");
        long id = ((Number) usuario.get("id")).longValue();
        try {
            assertEquals("cliente", usuario.get("rol"));
            assertFalse(usuario.containsKey("clave"));
            String token = login((String) datos.get("correo"));
            assertEquals(3, token.split("\\.").length);
            Respuesta cuenta = llamar(HttpMethod.GET, "/api/clientes/" + id, token, null);
            assertEquals(200, cuenta.status());
            assertEquals(false, cuenta.json().get("cuentaCorrienteHabilitada"));
            assertEquals(409, llamar(HttpMethod.POST, "/api/auth/registro", null, datos).status());
        } finally { darDeBaja(id); }
    }

    @Test
    void registroRechazaRolAdminDominioInvalidoYCamposVacios() {
        Map<String, Object> datos = datosUsuario();
        datos.put("rol", "admin");
        assertEquals(400, llamar(HttpMethod.POST, "/api/auth/registro", null, datos).status());
        datos.remove("rol"); datos.put("correo", "prueba@example.com");
        assertEquals(409, llamar(HttpMethod.POST, "/api/auth/registro", null, datos).status());
        assertEquals(400, llamar(HttpMethod.POST, "/api/auth/registro", null, Map.of()).status());
    }

    @Test
    void adminPuedeCrearEditarYDarDeBajaUnUsuario() {
        Map<String, Object> datos = datosUsuario();
        datos.put("rol", "vendedor");
        long id = crearUsuario(datos);
        boolean eliminado = false;
        try {
            String token = login((String) datos.get("correo"));
            datos.remove("clave"); datos.put("nombre", "Editado");
            Respuesta r = llamar(HttpMethod.PUT, "/api/usuarios/" + id, tokenAdmin, datos);
            assertEquals(200, r.status());
            assertEquals("Editado", r.json().get("nombre"));
            assertFalse(r.json().containsKey("clave"));
            login((String) datos.get("correo"));
            darDeBaja(id); eliminado = true;
            assertEquals(404, llamar(HttpMethod.GET, "/api/usuarios/" + id, tokenAdmin, null).status());
            assertEquals(401, llamar(HttpMethod.POST, "/api/auth/login", null,
                    Map.of("correo", datos.get("correo"), "clave", "1234")).status());
            assertEquals(401, llamar(HttpMethod.GET, "/api/usuarios", token, null).status());
        } finally { if (!eliminado) darDeBaja(id); }
    }

    @Test
    void correoYRutRepetidosSeRechazanSinCrearOtroUsuario() {
        Map<String, Object> datos = datosUsuario(); datos.put("rol", "vendedor");
        long id = crearUsuario(datos);
        try {
            int antes = llamar(HttpMethod.GET, "/api/usuarios", tokenAdmin, null).lista().size();
            Map<String, Object> otro = datosUsuario(); otro.put("rol", "vendedor");
            otro.put("correo", datos.get("correo"));
            assertEquals(409, llamar(HttpMethod.POST, "/api/usuarios", tokenAdmin, otro).status());
            otro.put("correo", "otro." + UUID.randomUUID() + "@gmail.com"); otro.put("rut", datos.get("rut"));
            assertEquals(409, llamar(HttpMethod.POST, "/api/usuarios", tokenAdmin, otro).status());
            assertEquals(antes, llamar(HttpMethod.GET, "/api/usuarios", tokenAdmin, null).lista().size());
        } finally { darDeBaja(id); }
    }

    @Test
    void sinTokenOConClaveIncorrectaOTokenAlteradoNoHayAcceso() {
        assertEquals(401, llamar(HttpMethod.GET, "/api/usuarios", null, null).status());
        assertEquals(401, llamar(HttpMethod.POST, "/api/auth/login", null,
                Map.of("correo", "admin@duoc.cl", "clave", "incorrecta")).status());
        String alterado = (tokenAdmin.startsWith("a") ? "b" : "a") + tokenAdmin.substring(1);
        assertEquals(401, llamar(HttpMethod.GET, "/api/usuarios", alterado, null).status());
    }

    @Test
    void vendedorYClienteNoPuedenGestionarUsuarios() {
        for (String token : List.of(tokenVendedor, tokenJuan)) {
            assertEquals(403, llamar(HttpMethod.GET, "/api/usuarios", token, null).status());
            Map<String, Object> datos = datosUsuario(); datos.put("rol", "vendedor");
            assertEquals(403, llamar(HttpMethod.POST, "/api/usuarios", token, datos).status());
        }
    }

    @Test
    void noSePuedeQuitarAlUltimoAdministrador() {
        Respuesta lista = llamar(HttpMethod.GET, "/api/usuarios", tokenAdmin, null);
        assertEquals(200, lista.status());
        var admins = lista.lista().stream().filter(u -> "admin".equals(u.get("rol"))).toList();
        //no se cambia el rol ni se elimina una cuenta inicial si hay varios administradores
        assumeTrue(admins.size() == 1, "Esta regla necesita exactamente un administrador activo");
        Map<String, Object> admin = admins.getFirst();
        long id = ((Number) admin.get("id")).longValue();
        assertEquals(409, llamar(HttpMethod.DELETE, "/api/usuarios/" + id, tokenAdmin, null).status());
        Map<String, Object> datos = new LinkedHashMap<>();
        for (String campo : List.of("rut", "nombre", "apellidos", "correo", "region", "comuna", "direccion"))
            datos.put(campo, admin.get(campo));
        datos.put("rol", "vendedor");
        assertEquals(409, llamar(HttpMethod.PUT, "/api/usuarios/" + id, tokenAdmin, datos).status());
    }

}
