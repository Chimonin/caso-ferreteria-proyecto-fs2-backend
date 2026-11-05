package Fullstackeando.Aura.productos;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:productos_test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "jwt.secret=c29sby1wYXJhLXBydWViYXMtand0LXNpbi12YWxvci1yZWFsLTAxMjM0NTY=",
        "servicios.internal-key=clave-interna-solo-para-pruebas-0123456789"
})
@AutoConfigureMockMvc
class ProductosIntegrationTest {
    private static final String INTERNA = "clave-interna-solo-para-pruebas-0123456789";
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate db;

    @BeforeEach
    void preparar() {
        db.update("DELETE FROM producto WHERE codigo = 'TEST001'");
        db.update("UPDATE producto SET stock = 10, activo = TRUE WHERE codigo = 'MC001'");
    }

    private String token(String rol) {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode("c29sby1wYXJhLXBydWViYXMtand0LXNpbi12YWxvci1yZWFsLTAxMjM0NTY="));
        return Jwts.builder().subject("prueba@duoc.cl").claim("id", 1).claim("roles", List.of("ROLE_" + rol))
                .expiration(new Date(System.currentTimeMillis() + 60000)).signWith(key).compact();
    }

    private String producto() {
        return "{\"codigo\":\"TEST001\",\"nombre\":\"Prueba\",\"categoria\":\"Construcción\","
                + "\"subcategoria\":\"Cementos\",\"marca\":\"Prueba\",\"unidad\":\"Saco\","
                + "\"precio\":5000,\"stock\":10,\"stockMinimo\":3}";
    }

    @Test
    void catalogoEsPublicoPeroCrearRequiereAdmin() throws Exception {
        mvc.perform(get("/api/productos")).andExpect(status().isOk());
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(producto()))
                .andExpect(status().isUnauthorized());
        for (String rol : List.of("VENDEDOR", "CLIENTE")) {
            mvc.perform(post("/api/productos").header("Authorization", "Bearer " + token(rol))
                            .contentType(MediaType.APPLICATION_JSON).content(producto()))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void adminCompletaCrudYLaBajaConservaElRegistro() throws Exception {
        String admin = "Bearer " + token("ADMIN");
        mvc.perform(post("/api/productos").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(producto()))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/productos").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(producto()))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/productos/TEST001").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(producto().replace("Prueba", "Editado")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Editado"));
        mvc.perform(patch("/api/productos/TEST001").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"precio\":6000,\"stock\":2}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.stockBajo").value(true));
        mvc.perform(delete("/api/productos/TEST001").header("Authorization", admin))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/productos/TEST001")).andExpect(status().isNotFound());
        assertEquals(1, db.queryForObject("SELECT COUNT(*) FROM producto WHERE codigo = 'TEST001' AND activo = FALSE", Integer.class));
    }

    @Test
    void vendedorSoloPuedeModificarStockNoPrecio() throws Exception {
        String vendedor = "Bearer " + token("VENDEDOR");
        mvc.perform(patch("/api/productos/MC001/stock").header("Authorization", vendedor)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"stock\":20}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.stock").value(20));
        mvc.perform(patch("/api/productos/MC001").header("Authorization", vendedor)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"precio\":1}"))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/productos/MC001/stock").header("Authorization", vendedor)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"stock\":20,\"precio\":1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/productos/MC001").header("Authorization", vendedor))
                .andExpect(status().isForbidden());
    }

    @Test
    void validacionesRechazanCamposVaciosNegativosYDecimales() throws Exception {
        String admin = "Bearer " + token("ADMIN");
        mvc.perform(post("/api/productos").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        for (String datos : List.of("{\"stock\":-1}", "{\"stock\":1.5}", "{\"stock\":null}")) {
            mvc.perform(patch("/api/productos/MC001/stock").header("Authorization", admin)
                            .contentType(MediaType.APPLICATION_JSON).content(datos))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void stockInternoAgrupaDuplicadosYCompensaInclusoSiProductoFueDadoDeBaja() throws Exception {
        String items = "[{\"codigo\":\"MC001\",\"cantidad\":2},{\"codigo\":\"MC001\",\"cantidad\":3}]";
        mvc.perform(post("/internal/productos/descontar-stock").contentType(MediaType.APPLICATION_JSON).content(items))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/internal/productos/descontar-stock").header("X-Internal-Key", INTERNA)
                        .contentType(MediaType.APPLICATION_JSON).content(items))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].cantidad").value(5));
        assertEquals(5, db.queryForObject("SELECT stock FROM producto WHERE codigo='MC001'", Integer.class));
        db.update("UPDATE producto SET activo=FALSE WHERE codigo='MC001'");
        mvc.perform(post("/internal/productos/reponer-stock").header("X-Internal-Key", INTERNA)
                        .contentType(MediaType.APPLICATION_JSON).content(items))
                .andExpect(status().isNoContent());
        assertEquals(10, db.queryForObject("SELECT stock FROM producto WHERE codigo='MC001'", Integer.class));
    }

    @Test
    void pedidoInvalidoNoDescuentaParcialmenteYNoPermiteSobreventa() throws Exception {
        for (String items : List.of(
                "[{\"codigo\":\"MC001\",\"cantidad\":2},{\"codigo\":\"ZZNOEXISTE\",\"cantidad\":1}]",
                "[{\"codigo\":\"MC001\",\"cantidad\":6},{\"codigo\":\"MC001\",\"cantidad\":6}]")) {
            mvc.perform(post("/internal/productos/descontar-stock").header("X-Internal-Key", INTERNA)
                            .contentType(MediaType.APPLICATION_JSON).content(items))
                    .andExpect(status().isConflict());
            assertEquals(10, db.queryForObject("SELECT stock FROM producto WHERE codigo='MC001'", Integer.class));
        }
        mvc.perform(post("/internal/productos/descontar-stock").header("X-Internal-Key", INTERNA)
                        .contentType(MediaType.APPLICATION_JSON).content("[{\"codigo\":\"MC001\",\"cantidad\":-1}]"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/internal/productos/descontar-stock").header("X-Internal-Key", INTERNA)
                        .contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dosComprasSimultaneasNoPuedenUsarElMismoStock() throws Exception {
        try (var ejecutor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var salida = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.Callable<Integer> comprar = () -> {
                salida.await();
                return mvc.perform(post("/internal/productos/descontar-stock").header("X-Internal-Key", INTERNA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("[{\"codigo\":\"MC001\",\"cantidad\":10}]"))
                        .andReturn().getResponse().getStatus();
            };
            var primera = ejecutor.submit(comprar);
            var segunda = ejecutor.submit(comprar);
            salida.countDown();
            var estados = new ArrayList<>(List.of(primera.get(10, java.util.concurrent.TimeUnit.SECONDS),
                    segunda.get(10, java.util.concurrent.TimeUnit.SECONDS)));
            Collections.sort(estados);
            assertEquals(List.of(200, 409), estados);
            assertEquals(0, db.queryForObject("SELECT stock FROM producto WHERE codigo='MC001'", Integer.class));
        }
    }
}
