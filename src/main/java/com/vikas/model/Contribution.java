package com.vikas.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

import java.time.Instant;

@Data
public class Contribution {
    private String id;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
            timezone = "UTC")
    private Instant date;

    private String type;
    private Integer count;
}
