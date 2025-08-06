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
    @JsonProperty("avatar_url")
    private String avatarUrl;
    private String bio;
    @JsonProperty("public_repos")
    private int publicReposCount;
    @JsonProperty("followers")
    private int followersCount;
    @JsonProperty("following")
    private int followingCount;
//    "total_private_repos"
    // private String location;
    private LocalDate created_at;
    private LocalDate updated_at;

}
