package com.vikas.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

import java.time.Instant;

@Data
public class Repository {
    private String id;
    private String name;
    private String description;
    private String language;
    private Integer stargazerCount;
    private Integer forkCount;
    private Boolean isPrivate;

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
}
