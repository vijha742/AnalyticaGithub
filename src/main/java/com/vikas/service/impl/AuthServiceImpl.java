package com.vikas.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.vikas.exception.AuthException;
import com.vikas.model.User;
import com.vikas.service.JWTService;
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

import com.vikas.dto.AuthDTO;
import com.vikas.dto.AuthResponse;
import com.vikas.service.AuthService;
import com.vikas.dto.SocialLoginRequest;
import com.vikas.service.GitHubService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

	private static final String GITHUB_USER_API_URL = "https://api.github.com/user";
	private final RestTemplate restTemplate;
	private final JWTService jwtService;
	private final GitHubService gitHubService;

	@Override
	public AuthDTO validate(String githubAccessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(githubAccessToken);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		HttpEntity<String> entity = new HttpEntity<>(headers);

		try {
			ResponseEntity<AuthDTO> response = restTemplate.exchange(GITHUB_USER_API_URL, HttpMethod.GET, entity,
					AuthDTO.class);

			if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
				return response.getBody();
			} else {
				throw new AuthException("Failed to validate GitHub token: GitHub API returned " + response.getStatusCode());
			}
		} catch (HttpClientErrorException e) {
			log.error("HttpClientErrorException during GitHub token validation: {} - {}",
					e.getStatusCode(),
					e.getResponseBodyAsString());

			if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
				throw new AuthException("Invalid GitHub token provided.", e);
			} else {
				throw new AuthException("Error communicating with GitHub API: " +
						e.getMessage(), e);
			}
		} catch (Exception e) {
			log.error("An unexpected error occurred during GitHub token validation: {}",
					e.getMessage(), e);
			throw new AuthException("An unexpected error occurred during GitHub token validation.", e);
		}
	}

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

		if (!verifiedGithubUser.getName().equals(request.getUserObject().getName())) {
			log.error("Mismatch between frontend GitHub UserName ({}) and verified GitHub UserName ({})",
					request.getUserObject().getName(), verifiedGithubUser.getName());
			throw new AuthException("GitHub user ID mismatch. Potential tampering detected.");
		}

		User user = gitHubService.findOrCreateUser(verifiedGithubUser);

		String jwtToken = jwtService.generateToken(user);
		String refreshToken = jwtService.generateRefreshToken(user);
		// This code creates a UsernamePasswordAuthenticationToken for the authenticated
		// user and sets it in the Spring Security context. This step is needed to mark
		// the user as authenticated for the current request, allowing Spring Security
		// to recognize the user and apply authorization rules. Without this, the user
		// would not be considered logged in by the application, and protected endpoints
		// would not be accessible.
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				user, null, Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		return AuthResponse.builder()
				.jwtToken(jwtToken)
				.refreshToken(refreshToken)
				.message("Authentication successful")
				.build();
	}

	@Override
	public AuthResponse refreshAccessToken(String refreshToken) throws AuthException {
		if (refreshToken == null || refreshToken.isEmpty()) {
			throw new AuthException("Refresh token is missing.");
		}
		final String username = jwtService.extractUsername(refreshToken);
		if (username != null) {

			User userDetails = gitHubService.findUser(username);
			if (jwtService.isTokenValid(refreshToken, userDetails)) {
				String newJwtToken = jwtService.generateToken(userDetails);
				return AuthResponse.builder()
						.jwtToken(newJwtToken)
						.refreshToken(refreshToken) // Return the same refresh token
						.message("Access token refreshed successfully")
						.build();
			}
		}
		throw new AuthException("Invalid or expired refresh token.");
	}

}
