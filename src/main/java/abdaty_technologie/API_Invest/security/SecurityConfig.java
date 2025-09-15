package abdaty_technologie.API_Invest.security;

<<<<<<< HEAD
import org.springframework.beans.factory.annotation.Autowired;
=======
>>>>>>> origin/feature/EDP
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
<<<<<<< HEAD
import org.springframework.security.config.http.SessionCreationPolicy;
=======
>>>>>>> origin/feature/EDP
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
<<<<<<< HEAD
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
=======
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;
>>>>>>> origin/feature/EDP

@Configuration
@EnableWebSecurity
public class SecurityConfig {

<<<<<<< HEAD
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Endpoints publics (sans le context-path car Spring Security les voit sans)
                .requestMatchers("/auth/login", "/auth/register", "/auth/forgot-password", "/auth/reset-password", "/divisions/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                
                // Endpoints protégés par rôle
                // .requestMatchers("/utilisateurs/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "USER")
                // .requestMatchers("/divisions/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "USER")
                // .requestMatchers("/persons/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                // .requestMatchers("/paiements/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "USER")
                // Tous les autres endpoints nécessitent une authentification
                .anyRequest().authenticated()
            );

        // Ajouter le filtre JWT avant le filtre d'authentification par nom d'utilisateur/mot de passe
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
=======
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
>>>>>>> origin/feature/EDP

        return http.build();
    }

<<<<<<< HEAD
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
=======
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
>>>>>>> origin/feature/EDP
}
