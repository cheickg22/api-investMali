package abdaty_technologie.API_Invest.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:}")
    private String allowedOrigins;

    private final RateLimitingFilter rateLimitingFilter;
    private final SecurityMetricFilter securityMetricFilter;

    public SecurityConfig(RateLimitingFilter rateLimitingFilter, SecurityMetricFilter securityMetricFilter) {
        this.rateLimitingFilter = rateLimitingFilter;
        this.securityMetricFilter = securityMetricFilter;
    }

    // Chaîne de filtres Spring Security
    // - CORS: pris depuis app.cors.allowed-origins (application.yml)
    // - CSRF: désactivé pour API stateless
    // - RateLimitingFilter: limite simple in-memory par IP
    // - SecurityMetricFilter: temps des requêtes en DEBUG
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/**").permitAll()
                // Swagger / OpenAPI resources
                .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().permitAll()
            )
            // Désactiver X-Frame-Options pour H2 console (API de config non-dépréciée)
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));
        
        http.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(securityMetricFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Source CORS basée sur la config YAML
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            List<String> origins = Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList();
            config.setAllowedOrigins(origins);
        } else {
            config.setAllowedOrigins(List.of("*"));
        }
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
