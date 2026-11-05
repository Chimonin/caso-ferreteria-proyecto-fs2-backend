package Fullstackeando.Aura.usuarios.security;

import Fullstackeando.Aura.usuarios.repository.UsuarioRepository;
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
        , "servicios.internal-key=clave-interna-solo-para-pruebas-0123456789"
})
@AutoConfigureMockMvc
@Transactional
class JwtIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtService jwtService;
    @Autowired Fullstackeando.Aura.usuarios.repository.ClienteRepository clientes;

    private java.util.Map<String, Object> datosUsuario(String rol) {
        var datos = new java.util.LinkedHashMap<String, Object>();
        datos.put("rut", "176543219"); datos.put("nombre", "Prueba"); datos.put("apellidos", "Usuario");
        datos.put("correo", "nuevo@gmail.com"); datos.put("clave", "1234"); datos.put("rol", rol);
        datos.put("region", "Coquimbo"); datos.put("comuna", "La Serena"); datos.put("direccion", "Calle 123");
        datos.put("tipoCliente", "contratista"); datos.put("cuentaCorrienteHabilitada", true);
        return datos;
    }

    @Test
    void registroPublicoCreaClienteSinCreditoYEntregaJwt() throws Exception {
        var datos = datosUsuario("cliente");
        datos.remove("rol"); datos.remove("cuentaCorrienteHabilitada");
        String cuerpo = mapper.writeValueAsString(datos);
        var respuesta = mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.usuario.rol").value("cliente"))
                .andReturn().getResponse().getContentAsString();
        var json = mapper.readTree(respuesta);
        long id = json.get("usuario").get("id").asLong();
        mvc.perform(get("/api/clientes/" + id).header("Authorization", "Bearer " + json.get("token").asText()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.cuentaCorrienteHabilitada").value(false));
        login("nuevo@gmail.com");
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict());
    }

    @Test
    void registroRechazaEscalarRolDominioNoPermitidoYComunaInvalida() throws Exception {
        var datos = datosUsuario("admin");
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(datos))).andExpect(status().isBadRequest());
        datos.remove("rol"); datos.remove("cuentaCorrienteHabilitada");
        datos.put("correo", "nuevo@example.com");
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(datos))).andExpect(status().isConflict());
        datos.put("correo", "nuevo@gmail.com"); datos.put("comuna", "Santiago");
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(datos))).andExpect(status().isConflict());
    }

    @Test
    void adminCreaEditaYDaDeBajaUsuarioSinPerderSuClave() throws Exception {
        String admin = login("admin@duoc.cl");
        var datos = datosUsuario("vendedor");
        String respuesta = mvc.perform(post("/api/usuarios").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(datos)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(respuesta).get("id").asLong();
        String token = login("nuevo@gmail.com");
        datos.put("nombre", "Editado"); datos.remove("clave");
        mvc.perform(put("/api/usuarios/" + id).header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(datos)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Editado"));
        login("nuevo@gmail.com");
        mvc.perform(delete("/api/usuarios/" + id).header("Authorization", "Bearer " + admin))
                .andExpect(status().isNoContent());
        assertFalse(usuarios.findById(id).orElseThrow().isActivo());
        mvc.perform(get("/api/clientes").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }

    @Test
    void adminNoPuedeEliminarNiCambiarRolDelUltimoAdministrador() throws Exception {
        String token = login("admin@duoc.cl");
        mvc.perform(delete("/api/usuarios/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
        var datos = datosUsuario("vendedor"); datos.put("rut", "111111111"); datos.put("correo", "admin@duoc.cl");
        mvc.perform(put("/api/usuarios/1").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(datos)))
                .andExpect(status().isConflict());
    }

    @Test
    void clientesConDeudaNoPuedenDarseDeBajaNiPerderCuentaCorriente() throws Exception {
        String token = login("admin@duoc.cl");
        for (String ruta : new String[]{"/api/clientes/4", "/api/usuarios/4"}) {
            mvc.perform(delete(ruta).header("Authorization", "Bearer " + token)).andExpect(status().isConflict());
        }
        mvc.perform(put("/api/clientes/4").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoCliente\":\"particular\",\"cuentaCorrienteHabilitada\":false}"))
                .andExpect(status().isConflict());
    }

    @Test
    void clienteCrudSoloAdminYParticularesNoObtienenCredito() throws Exception {
        String admin = login("admin@duoc.cl");
        String cliente = login("juan.perez@gmail.com");
        String datos = "{\"tipoCliente\":\"particular\",\"cuentaCorrienteHabilitada\":true}";
        mvc.perform(put("/api/clientes/3").header("Authorization", "Bearer " + cliente)
                        .contentType(MediaType.APPLICATION_JSON).content(datos)).andExpect(status().isForbidden());
        mvc.perform(put("/api/clientes/3").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content(datos))
                .andExpect(status().isOk()).andExpect(jsonPath("$.cuentaCorrienteHabilitada").value(false));
        clientes.deleteById(3L); clientes.flush();
        mvc.perform(post("/api/clientes").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":3,\"tipoCliente\":\"particular\",\"cuentaCorrienteHabilitada\":false}"))
                .andExpect(status().isCreated());
        mvc.perform(delete("/api/clientes/3").header("Authorization", "Bearer " + admin))
                .andExpect(status().isNoContent());
    }

    @Test
    void deudaInternaExigeClaveDeServicioYNoPermiteSobrepago() throws Exception {
        mvc.perform(get("/internal/clientes/4")).andExpect(status().isUnauthorized());
        mvc.perform(get("/internal/clientes/4").header("Authorization", "Bearer " + login("admin@duoc.cl")))
                .andExpect(status().isUnauthorized());
        String clave = "clave-interna-solo-para-pruebas-0123456789";
        mvc.perform(patch("/internal/clientes/4/sumar-deuda").header("X-Internal-Key", clave)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"monto\":1000}"))
                .andExpect(status().isNoContent());
        mvc.perform(get("/internal/clientes/4").header("X-Internal-Key", clave))
                .andExpect(status().isOk()).andExpect(jsonPath("$.saldoAdeudado").value(33000));
        mvc.perform(patch("/internal/clientes/4/restar-deuda").header("X-Internal-Key", clave)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"monto\":1000}"))
                .andExpect(status().isNoContent());
        mvc.perform(patch("/internal/clientes/4/restar-deuda").header("X-Internal-Key", clave)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"monto\":50000}"))
                .andExpect(status().isConflict());
    }

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
