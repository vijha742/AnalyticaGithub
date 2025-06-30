package com.vikas.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String githubUsername;

    @Column(nullable = false)
    private String name;

    private String email;
    private String avatarUrl;
    private String bio;
    private Integer followersCount;
    private Integer followingCount;
    private Integer publicReposCount;
    private Integer totalContributions;
    
    @Column(name = "last_updated")
    private Instant lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = Instant.now();
    }
} 