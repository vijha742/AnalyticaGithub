package com.vikas.service.impl;

import com.vikas.dto.AuthDTO;
import com.vikas.dto.AuthResponse;
import com.vikas.dto.SocialLoginRequest;
import com.vikas.exception.AuthException;
import com.vikas.model.User;
import com.vikas.repository.UserRepository;
import com.vikas.service.AuthService;
import com.vikas.service.GitHubService;
import com.vikas.service.JWTService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final String GITHUB_USER_API_URL = "https://api.github.com/user";

    private final RestTemplate restTemplate;
    private final JWTService jwtService;
    private final GitHubService gitHubService;
    private final UserRepository userRepository;

    @Value("${jwt.refresh.expiration.ms}")
    private long refreshExpiration;

    // -------------------------------------------------------------------------
    // GitHub token validation
    // -------------------------------------------------------------------------

    @Override
    public AuthDTO validate(String githubAccessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(githubAccessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<AuthDTO> response =
                    restTemplate.exchange(
                            GITHUB_USER_API_URL, HttpMethod.GET, entity, AuthDTO.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new AuthException(
                        "Failed to validate GitHub token: GitHub API returned "
                                + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            log.error(
                    "HttpClientErrorException during GitHub token validation: {} - {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());

            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new AuthException("Invalid GitHub token provided.", e);
            } else {
                throw new AuthException(
                        "Error communicating with GitHub API: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error(
                    "An unexpected error occurred during GitHub token validation: {}",
                    e.getMessage(),
                    e);
            throw new AuthException(
                    "An unexpected error occurred during GitHub token validation.", e);
        }
    }

    // -------------------------------------------------------------------------
    // Sign-in: validate GitHub token → issue JWT + refresh token
    // -------------------------------------------------------------------------

    @Override
    public AuthResponse authenticate(SocialLoginRequest request) throws AuthException {
        if (request.getGithubToken() == null || request.getGithubToken().isEmpty()) {
            throw new AuthException("GitHub token is missing in the request.");
        }

        if (request.getUserObject() == null || request.getUserObject().getId() == null) {
            throw new AuthException("GitHub user ID is missing in the request.");
        }

        AuthDTO verifiedGithubUser;
        try {
            verifiedGithubUser = validate(request.getGithubToken());
        } catch (AuthException e) {
            throw new AuthException("Invalid GitHub token or failed to verify with GitHub.", e);
        }

        // Guard against a frontend sending mismatched identity
        if (!verifiedGithubUser.getId().equals(Long.parseLong(request.getUserObject().getGithubId()))) {
            log.error(
                    "Mismatch between frontend GitHub User ID ({}) and verified GitHub User ID ({})",
                    request.getUserObject().getGithubId(),
                    verifiedGithubUser.getId());
            throw new AuthException("GitHub user ID mismatch. Potential tampering detected.");
        }

        User user = gitHubService.findOrCreateUser(verifiedGithubUser);

        String jwtToken     = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Persist the refresh token so we can revoke it and detect replay on rotation
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiresAt(Instant.now().plusMillis(refreshExpiration));
        userRepository.save(user);

        // Mark the request as authenticated for the current Spring Security context
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return AuthResponse.builder()
                .jwtToken(jwtToken)
                .refreshToken(refreshToken)
                .message("Authentication successful")
                .userData(user)
                .build();
    }

    // -------------------------------------------------------------------------
    // Token refresh: DB lookup → validate expiry → rotate refresh token
    // -------------------------------------------------------------------------

    @Override
    public AuthResponse refreshAccessToken(String refreshToken) throws AuthException {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new AuthException("Refresh token is missing.");
        }

        // Look up by stored token value — this is what gives us revocation.
        // If the token was invalidated (logged out, rotated away), it won't be
        // in the DB and we reject immediately without parsing the JWT.
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new AuthException("Invalid or revoked refresh token."));

        // Belt-and-suspenders: also check the expiry timestamp we stored
        if (user.getRefreshTokenExpiresAt() == null
                || user.getRefreshTokenExpiresAt().isBefore(Instant.now())) {
            // Clear stale token to keep DB clean
            user.setRefreshToken(null);
            user.setRefreshTokenExpiresAt(null);
            userRepository.save(user);
            throw new AuthException("Refresh token has expired. Please sign in again.");
        }

        // Rotate the refresh token — old token is invalidated immediately
        String newRefreshToken = jwtService.generateRefreshToken(user);
        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiresAt(Instant.now().plusMillis(refreshExpiration));
        userRepository.save(user);

        String newJwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .jwtToken(newJwtToken)
                .refreshToken(newRefreshToken)
                .message("Access token refreshed successfully")
                .build();
    }
}
