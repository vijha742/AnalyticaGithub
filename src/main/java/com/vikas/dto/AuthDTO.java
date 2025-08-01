package com.vikas.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * AuthDTO
 */
@Data
public class AuthDTO {

    @JsonProperty("login")
    private String userName;
    private String name;
    private String email;
    private String avatarUrl;
    private String bio;
    @JsonProperty("public_repos")
    private int publicReposCount;
    private int followersCount;
    private int followingCount;
    // private String location;
    private LocalDate created_at;
    private LocalDate updated_at;

}
