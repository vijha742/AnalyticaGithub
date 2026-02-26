package com.vikas.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

import lombok.Data;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "SuggestedUser_repo_data")
@Data
public class SuggestedGithubRepository {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String language;
    private Integer stargazerCount;
    private Integer forkCount;
    private Boolean isPrivate;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private SuggestedUser user;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
            timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
            timezone = "UTC")
    private Instant updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SuggestedGithubRepository)) return false;
        SuggestedGithubRepository that = (SuggestedGithubRepository) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
