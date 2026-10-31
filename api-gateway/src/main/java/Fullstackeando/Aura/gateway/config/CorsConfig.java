package Fullstackeando.Aura.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

//para que el navegador no bloquee las peticiones de React al backend
//va solo en el gateway, porque React nunca le habla directo a los microservicios
//se usa un filtro (y no addCorsMappings) porque el filtro revisa TODAS las peticiones
//antes de que el gateway las reenvie
@Configuration
public class CorsConfig {

    @Value("${gateway.cors.origen}")
    private String origenPermitido;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin(origenPermitido);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
