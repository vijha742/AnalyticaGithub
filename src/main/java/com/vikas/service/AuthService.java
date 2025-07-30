package com.vikas.service;

import com.vikas.dto.AuthDTO;
import com.vikas.exception.AuthException;
import com.vikas.model.User;
import com.vikas.dto.AuthResponse;
import com.vikas.dto.SocialLoginRequest;

public interface AuthService {

    AuthResponse authenticate(SocialLoginRequest request) throws AuthException;

    AuthDTO validate(String githubAccessToken);

    AuthResponse refreshAccessToken(String refreshToken);
}
