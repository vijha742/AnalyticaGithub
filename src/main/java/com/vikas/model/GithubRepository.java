package com.vikas.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "repo_data")
@Data
public class GithubRepository {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private UUID id;
        private String name;
        private String description;
        private String language;
        private Integer stargazerCount;
        private Integer forkCount;
        private Boolean isPrivate;
        @JdbcTypeCode(SqlTypes.JSON)
        private ReadmeQuality readmeData;
        @JdbcTypeCode(SqlTypes.JSON)
        private CodeMetrics codeData;
        @JdbcTypeCode(SqlTypes.JSON)
        private ActivityData activity;

        @ManyToOne
        private User user;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        private Instant createdAt;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        private Instant updatedAt;
}
