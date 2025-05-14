package com.vikas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AnalyticaGithubApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnalyticaGithubApplication.class, args);
    }
} 