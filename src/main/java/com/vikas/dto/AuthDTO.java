package com.vikas.dto;

import java.util.Date;

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
    private Date created_at;
    private Date updated_at;

}
