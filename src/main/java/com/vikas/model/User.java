package com.vikas.model;

import com.vikas.model.UserReadmeAnalysis;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class User {
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


//    private Integer totalContributionsThisYear;

    private LocalDate createdAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<GithubRepository> userRepository;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Contribution> contributions;

    @JdbcTypeCode(SqlTypes.JSON)
    private UserReadmeAnalysis userReadmeAnalysis;

    @JdbcTypeCode(SqlTypes.JSON)
    private TechnicalProfile technicalProfile;

    @JdbcTypeCode(SqlTypes.JSON)
    private TechTimeline userTech;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = Instant.now();
    }
}
