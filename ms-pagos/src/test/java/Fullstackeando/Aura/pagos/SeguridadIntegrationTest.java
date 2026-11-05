package Fullstackeando.Aura.pagos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:pagos_seguridad;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "jwt.secret=c29sby1wYXJhLXBydWViYXMtand0LXNpbi12YWxvci1yZWFsLTAxMjM0NTY=",
    "servicios.internal-key=clave-interna-solo-para-pruebas-0123456789"
})
@AutoConfigureMockMvc
@Transactional
class SeguridadIntegrationTest {
    @Autowired MockMvc mvc;

    private String token(String rol, long id) {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode("c29sby1wYXJhLXBydWViYXMtand0LXNpbi12YWxvci1yZWFsLTAxMjM0NTY="));
        return "Bearer " + Jwts.builder().subject("prueba@duoc.cl").claim("id", id).claim("roles", List.of("ROLE_" + rol))
                .expiration(new Date(System.currentTimeMillis() + 60000)).signWith(key).compact();
    }

    @Test
    void listadosExigenSesionYRol() throws Exception {
        mvc.perform(get("/api/pagos")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/pagos").header("Authorization", token("ADMIN", 1))).andExpect(status().isOk());
        mvc.perform(get("/api/pagos").header("Authorization", token("CLIENTE", 3))).andExpect(status().isForbidden());
        mvc.perform(get("/api/pagos").header("Authorization", token("VENDEDOR", 2)))
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteSoloPuedeConsultarSusPropiosRegistros() throws Exception {
        mvc.perform(get("/api/pagos/cliente/3").header("Authorization", token("CLIENTE", 3))).andExpect(status().isOk());
        mvc.perform(get("/api/pagos/cliente/4").header("Authorization", token("CLIENTE", 3))).andExpect(status().isForbidden());
    }

    @Test
    void noSePuedeRegistrarUnaOperacionParaOtroCliente() throws Exception {
        mvc.perform(post("/api/pagos").header("Authorization", token("CLIENTE", 3))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"clienteId\":4,\"monto\":1000}"))
                .andExpect(status().isForbidden());
    }

}
