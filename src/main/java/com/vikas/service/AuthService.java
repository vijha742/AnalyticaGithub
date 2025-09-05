package com.vikas.service;

import com.vikas.dto.AuthDTO;
import com.vikas.dto.AuthResponse;
import com.vikas.dto.SocialLoginRequest;
import com.vikas.exception.AuthException;

public interface AuthService {

    AuthResponse authenticate(SocialLoginRequest request) throws AuthException;

    AuthDTO validate(String githubAccessToken);

    AuthResponse refreshAccessToken(String refreshToken);
}
