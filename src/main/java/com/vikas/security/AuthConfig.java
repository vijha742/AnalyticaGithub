package com.vikas.security;

import com.vikas.service.GitHubService;
import com.vikas.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Base64;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AuthConfig {

    private final JWTAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userDetailsService;
                @Bean
                public SecurityFilterChain securityFilterChain(HttpSecurity http, @Value("http://localhost:3000") String targetUrl, CorsFilter corsFilter) throws Exception {
                                http.csrf(csrf -> csrf.disable())
                                                                .sessionManagement(session -> session
                                                                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                        .authenticationProvider(authenticationProvider())
                                        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                                                .authorizeHttpRequests(
                                                                                                authorize -> authorize
                                                                                                                                .requestMatchers("/api/public/**", "/auth/**")
                                                                                                                                .permitAll()
                                                                                                                                .requestMatchers("/api/admin/**")
                                                                                                                                .hasRole("ADMIN")
                                                                                                                                .anyRequest()
                                                                                                                                .authenticated());
                                return http.build();
                }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService((UserDetailsService) userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
