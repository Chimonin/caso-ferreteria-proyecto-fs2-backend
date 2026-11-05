package fullstackeando.aura.usuarios.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

//los endpoints internos usan una clave distinta al JWT de los usuarios
@Component
public class ServicioInternoFilter extends OncePerRequestFilter {
    private final byte[] clave;
    public ServicioInternoFilter(@Value("${servicios.internal-key}") String clave) {
        if (clave.length() < 32) throw new IllegalArgumentException("INTERNAL_API_KEY debe tener al menos 32 caracteres");
        this.clave = clave.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().substring(request.getContextPath().length()).startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String enviada = request.getHeader("X-Internal-Key");
        if (enviada == null || !MessageDigest.isEqual(clave, enviada.getBytes(StandardCharsets.UTF_8))) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"mensaje\":\"Credenciales de microservicio incorrectas\"}");
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "microservicio", null, List.of(new SimpleGrantedAuthority("ROLE_MICROSERVICIO"))));
        chain.doFilter(request, response);
    }
}
