package fullstackeando.aura.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

//para que el navegador no bloquee las peticiones de React al backend
//va solo en el gateway, porque React nunca le habla directo a los microservicios
//se usa un filtro (y no addCorsMappings) porque el filtro revisa TODAS las peticiones
//antes de que el gateway las reenvie
//acepta varios origenes separados por coma: 5173 (npm run dev) y 4173 (vite preview, el que usa Playwright)
@Configuration
public class CorsConfig {

    @Value("${gateway.cors.origen}")
    private String origenPermitido;

    //"http://localhost:5173,http://localhost:4173" -> ["http://localhost:5173", "http://localhost:4173"]
    private List<String> origenesPermitidos() {
        return Arrays.stream(origenPermitido.split(","))
                .map(String::trim)
                .filter(origen -> !origen.isEmpty())
                .toList();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origenesPermitidos());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
