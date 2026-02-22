package com.codebytes5.banking.accounts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService);
    }

    /**
     * IMPORTANTE: Deshabilita el registro automático del JwtAuthenticationFilter.
     * Al ser un @Bean, Spring Boot intenta registrarlo automáticamente en la cadena
     * de filtros global.
     * Esto causaba una doble ejecución (una global y otra en SecurityFilterChain).
     * Con enabled=false, tomamos control manual y solo se ejecuta donde nosotros
     * digamos.
     */
    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter filter) {
        org.springframework.boot.web.servlet.FilterRegistrationBean<JwtAuthenticationFilter> registration = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(
                filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                // Ejecutar filtro JWT antes de que Spring determine si el usuario es anónimo.
                // Esto soluciona problemas de 403 Forbidden en endpoints protegidos.
                .addFilterBefore(jwtAuthenticationFilter(),
                        org.springframework.security.web.authentication.AnonymousAuthenticationFilter.class);
        return http.build();
    }
}
