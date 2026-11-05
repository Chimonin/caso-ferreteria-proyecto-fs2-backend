package fullstackeando.aura.usuarios.security;

import fullstackeando.aura.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:jwt_test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "jwt.secret=c29sby1wYXJhLXBydWViYXMtand0LXNpbi12YWxvci1yZWFsLTAxMjM0NTY=",
        "jwt.expiration-ms=3600000"
})
@AutoConfigureMockMvc
@Transactional
class JwtIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtService jwtService;

    private String login(String correo) throws Exception {
        String respuesta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"clave\":\"1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.clave").doesNotExist())
                .andExpect(jsonPath("$.tipoToken").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(respuesta).get("token").asText();
    }

    @Test
    void migracionConservaElLoginConClavesBCrypt() throws Exception {
        var usuario = usuarios.findById(1L).orElseThrow();
        assertNotEquals("1234", usuario.getClave());
        assertTrue(encoder.matches("1234", usuario.getClave()));
        var claims = jwtService.validarToken(login("ADMIN@DUOC.CL"));
        assertEquals("admin@duoc.cl", claims.getSubject());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void loginRechazaClaveIncorrectaYDatosInvalidos() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@duoc.cl\",\"clave\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"invalido\",\"clave\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void administradorPuedeConsultarUsuariosYClientes() throws Exception {
        String token = login("admin@duoc.cl");
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].clave").doesNotExist());
        mvc.perform(get("/api/clientes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void clienteSoloConsultaSuPropiaCuenta() throws Exception {
        String token = login("ana.torres@gmail.com");
        mvc.perform(get("/api/clientes/4").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(4));
        for (String ruta : new String[]{"/api/clientes/3", "/api/clientes", "/api/usuarios"}) {
            mvc.perform(get(ruta).header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void vendedorNoPuedeGestionarUsuarios() throws Exception {
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + login("vendedor@duoc.cl")))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinTokenOConTokenAlteradoOVencidoResponde401() throws Exception {
        mvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
        String token = login("admin@duoc.cl");
        String alterado = (token.startsWith("a") ? "b" : "a") + token.substring(1);
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + alterado))
                .andExpect(status().isUnauthorized());
        //token firmado con la misma clave de pruebas, pero con vencimiento en el pasado
        var key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(
                "c29sby1wYXJhLXBydWViYXMtand0LXNpbi12YWxvci1yZWFsLTAxMjM0NTY="));
        String vencido = io.jsonwebtoken.Jwts.builder().subject("admin@duoc.cl").claim("id", 1)
                .expiration(new java.util.Date(System.currentTimeMillis() - 60000)).signWith(key).compact();
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + vencido))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioDesactivadoNoPuedeEntrarNiUsarUnTokenAnterior() throws Exception {
        String token = login("admin@duoc.cl");
        var usuario = usuarios.findById(1L).orElseThrow();
        usuario.setActivo(false);
        usuarios.saveAndFlush(usuario);
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@duoc.cl\",\"clave\":\"1234\"}"))
                .andExpect(status().isUnauthorized());
    }
}
