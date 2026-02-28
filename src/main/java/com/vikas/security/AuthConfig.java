package com.vikas.security;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AuthConfig {

    private final JWTAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("http://localhost:3000") String targetUrl,
            CorsFilter corsFilter)
            throws Exception {
        
        // CSRF token handler for proper token handling
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");
        
        http
                // Enable CSRF protection with cookie-based tokens
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(requestHandler)
                        // Disable CSRF only for public GET endpoints and auth endpoints
                        .ignoringRequestMatchers(
                                "/auth/signin", 
                                "/auth/refresh-jwt",
                                "/api/public/users/*/contrib-cal",
                                "/api/public/users/search",
                                "/api/public/users/test"
                        )
                )
                // Stateless session management for JWT
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Security headers
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; frame-ancestors 'none'"))
                        .frameOptions(frame -> frame.deny())
                        .xssProtection(xss -> xss.disable())
                        .contentTypeOptions(contentType -> contentType.disable())
                )
                // Add JWT authentication filter
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Authorization rules
                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        // Public documentation endpoints
                                        .requestMatchers(
                                                "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**")
                                        .permitAll()
                                        // Public read-only endpoints
                                        .requestMatchers("/auth/**")
                                        .permitAll()
                                        // Public GET endpoints only
                                        .requestMatchers("/api/public/users/*/contrib-cal", 
                                                        "/api/public/users/search",
                                                        "/api/public/users/test")
                                        .permitAll()
                                        // All other requests require authentication
                                        .anyRequest()
                                        .authenticated());
        return http.build();
    }
}
