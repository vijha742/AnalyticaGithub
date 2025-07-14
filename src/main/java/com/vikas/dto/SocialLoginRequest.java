package com.vikas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class SocialLoginRequest {
    private final String githubToken;
    private final Userdata userObject;
}

