package com.vikas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "suggested_users")
public class SuggestedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String githubUsername;
    @Column(nullable = false)
    private String name;
    private String email;
    private String avatarUrl;
    private String bio;
    private int followersCount;
    private int followingCount;
    private int publicReposCount;
    private Integer totalContributions;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User suggestedBy;

    @Column(nullable = false)
    private String team;

    @Column(nullable = false)
    private Instant suggestedAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    private int pullRequestsCount;
    private int issuesCount;
    private int commitsCount;

//    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<SuggestedGithubRepository> repositories;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Contribution> contributions;

    private Instant lastRefreshed;

    @PrePersist
    protected void onCreate() {
        suggestedAt = Instant.now();
    }

}
