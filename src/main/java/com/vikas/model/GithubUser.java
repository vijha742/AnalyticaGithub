package com.vikas.model;

import jakarta.persistence.*;

import lombok.Data;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Data
@Entity
@Table(name = "github_users")
public class GithubUser {
    @Id private String id;

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Repository> repositories;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Contribution> contributions;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = Instant.now();
    }
}

