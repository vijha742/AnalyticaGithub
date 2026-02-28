package com.vikas.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vikas.utils.QueryManager;

import java.time.Duration;

@Configuration
@EnableScheduling
public class AppConfig {

    @Bean
    public QueryManager queryManager() {
        return new QueryManager();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Configure RestTemplate with timeouts to prevent hanging
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))  // 5 seconds connection timeout
                .setReadTimeout(Duration.ofSeconds(10))     // 10 seconds read timeout
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }

    @Bean
    public CorsFilter corsFilter(@Value("${frontend.url}") String frontend) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        
        // Use pattern matching for better security
        config.addAllowedOriginPattern(frontend);
        
        // Explicitly define allowed headers instead of wildcard
        config.addAllowedHeader("Authorization");
        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("Accept");
        config.addAllowedHeader("X-Requested-With");
        config.addAllowedHeader("X-CSRF-TOKEN");
        
        // Explicitly define allowed methods instead of wildcard
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        
        // Expose headers that the frontend may need
        config.addExposedHeader("Authorization");
        config.addExposedHeader("X-CSRF-TOKEN");
        
        // Set max age for preflight requests (1 hour)
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
