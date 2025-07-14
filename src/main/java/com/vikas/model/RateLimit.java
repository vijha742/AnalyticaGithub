package com.vikas.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Data
public class RateLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "limit_count", nullable = false)
    private Integer limit;

    @Column(nullable = false)
    private Integer remaining;

    @Column(name = "reset_at", nullable = false)
    private Instant resetAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}