package com.vikas.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Entity
@Table(name = "github_users")
public class GithubUser {
    @Id
    private String id;

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

    @Data
    public static class Repository {
        private String id;
        private String name;
        private String description;
        private String language;
        private Integer stargazerCount;
        private Integer forkCount;
        private Boolean isPrivate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        private Instant createdAt;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        private Instant updatedAt;
    }

    @Data
    public static class Contribution {
        private String id;
        
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        private Instant date;
        private String type;
        private Integer count;
    }
}