package com.vikas.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "suggested_users")
public class SuggestedUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String githubUsername;

    @Column(nullable = false)
    private String suggestedBy;

    @Column(nullable = false)
    private String group;

    @Column(nullable = false)
    private Instant suggestedAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // GitHub User Data
    private String name;
    private String email;
    private String avatarUrl;
    private String bio;
    private Integer followersCount;
    private Integer followingCount;
    private Integer publicReposCount;
    private Integer totalContributions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Repository> repositories;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Contribution> contributions;

    private Instant lastRefreshed;

    @PrePersist
    protected void onCreate() {
        suggestedAt = Instant.now();
    }
}
