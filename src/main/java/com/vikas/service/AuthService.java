 package com.vikas.service;

import com.vikas.exception.AuthException;
import com.vikas.model.GithubUser;
import com.vikas.dto.AuthResponse;
import com.vikas.dto.SocialLoginRequest;

public interface AuthService {

    AuthResponse authenticate(SocialLoginRequest request) throws AuthException;
    GithubUser validate(String githubAccessToken);

    AuthResponse refreshAccessToken(String refreshToken);
}
