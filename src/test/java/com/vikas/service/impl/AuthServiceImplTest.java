package com.vikas.service.impl;

import com.vikas.dto.AuthDTO;
import com.vikas.dto.SocialLoginRequest;
import com.vikas.dto.Userdata;
import com.vikas.exception.AuthException;
import com.vikas.model.User;
import com.vikas.service.GitHubService;
import com.vikas.service.JWTService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JWTService jwtService;

    @Mock
    private GitHubService gitHubService;

    @InjectMocks
    private AuthServiceImpl authService;

    private AuthDTO verifiedGithubUser;
    private SocialLoginRequest validRequest;
    private SocialLoginRequest tamperedRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Set up verified GitHub user (from GitHub API)
        verifiedGithubUser = new AuthDTO();
        verifiedGithubUser.setId(123456L);
        verifiedGithubUser.setUserName("validuser");
        verifiedGithubUser.setName("Valid User");
        verifiedGithubUser.setEmail("valid@example.com");

        // Set up valid request (matches verified user)
        Userdata validUserdata = Userdata.builder()
                .githubId("123456")
                .name("Valid User")
                .email("valid@example.com")
                .build();

        validRequest = SocialLoginRequest.builder()
                .githubToken("valid-token")
                .userObject(validUserdata)
                .build();

        // Set up tampered request (different GitHub ID)
        Userdata tamperedUserdata = Userdata.builder()
                .githubId("999999") // Different ID - potential tampering!
                .name("Valid User")
                .email("valid@example.com")
                .build();

        tamperedRequest = SocialLoginRequest.builder()
                .githubToken("valid-token")
                .userObject(tamperedUserdata)
                .build();

        testUser = new User();
        testUser.setGithubId(123456L);
        testUser.setGithubUsername("validuser");
        testUser.setName("Valid User");
    }

    @Test
    void authenticate_shouldSucceedWithValidGithubId() {
        // Arrange
        ResponseEntity<AuthDTO> response = new ResponseEntity<>(verifiedGithubUser, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                any(HttpEntity.class), 
                eq(AuthDTO.class)))
                .thenReturn(response);
        
        when(gitHubService.findOrCreateUser(any(AuthDTO.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> authService.authenticate(validRequest));

        verify(gitHubService).findOrCreateUser(any(AuthDTO.class));
        verify(jwtService).generateToken(any(User.class));
        verify(jwtService).generateRefreshToken(any(User.class));
    }

    @Test
    void authenticate_shouldDetectGithubIdTampering() {
        // Arrange
        ResponseEntity<AuthDTO> response = new ResponseEntity<>(verifiedGithubUser, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                any(HttpEntity.class), 
                eq(AuthDTO.class)))
                .thenReturn(response);

        // Act & Assert
        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.authenticate(tamperedRequest);
        });

        assertEquals("GitHub user ID mismatch. Potential tampering detected.", exception.getMessage());

        // Verify that user creation and token generation were never called
        verify(gitHubService, never()).findOrCreateUser(any(AuthDTO.class));
        verify(jwtService, never()).generateToken(any(User.class));
        verify(jwtService, never()).generateRefreshToken(any(User.class));
    }

    @Test
    void authenticate_shouldNotAcceptNameBasedAuthentication() {
        // Arrange - Create a scenario where names match but IDs don't
        Userdata userdataWithSameName = Userdata.builder()
                .githubId("999999") // Different ID
                .name("Valid User") // Same name as verified user
                .email("valid@example.com")
                .build();

        SocialLoginRequest requestWithMatchingName = SocialLoginRequest.builder()
                .githubToken("valid-token")
                .userObject(userdataWithSameName)
                .build();

        ResponseEntity<AuthDTO> response = new ResponseEntity<>(verifiedGithubUser, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                any(HttpEntity.class), 
                eq(AuthDTO.class)))
                .thenReturn(response);

        // Act & Assert - Should fail even though names match
        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.authenticate(requestWithMatchingName);
        });

        assertEquals("GitHub user ID mismatch. Potential tampering detected.", exception.getMessage());

        // Verify that the authentication failed despite matching names
        verify(gitHubService, never()).findOrCreateUser(any(AuthDTO.class));
    }

    @Test
    void authenticate_shouldValidateIdAsLong() {
        // Arrange - Test that we correctly parse and compare IDs as Long values
        verifiedGithubUser.setId(9223372036854775807L); // Max Long value

        Userdata userdataWithMaxLong = Userdata.builder()
                .githubId("9223372036854775807")
                .name("Valid User")
                .email("valid@example.com")
                .build();

        SocialLoginRequest requestWithMaxLong = SocialLoginRequest.builder()
                .githubToken("valid-token")
                .userObject(userdataWithMaxLong)
                .build();

        testUser.setGithubId(9223372036854775807L);

        ResponseEntity<AuthDTO> response = new ResponseEntity<>(verifiedGithubUser, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                any(HttpEntity.class), 
                eq(AuthDTO.class)))
                .thenReturn(response);
        
        when(gitHubService.findOrCreateUser(any(AuthDTO.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        // Act & Assert - Should handle large Long values correctly
        assertDoesNotThrow(() -> authService.authenticate(requestWithMaxLong));

        verify(gitHubService).findOrCreateUser(any(AuthDTO.class));
    }

    @Test
    void authenticate_shouldRejectNullGithubId() {
        // Arrange
        Userdata userdataWithNullId = Userdata.builder()
                .githubId(null)
                .name("Valid User")
                .email("valid@example.com")
                .build();

        SocialLoginRequest requestWithNullId = SocialLoginRequest.builder()
                .githubToken("valid-token")
                .userObject(userdataWithNullId)
                .build();

        ResponseEntity<AuthDTO> response = new ResponseEntity<>(verifiedGithubUser, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(), 
                eq(HttpMethod.GET), 
                any(HttpEntity.class), 
                eq(AuthDTO.class)))
                .thenReturn(response);

        // Act & Assert - Should fail with null ID
        assertThrows(Exception.class, () -> {
            authService.authenticate(requestWithNullId);
        });

        verify(gitHubService, never()).findOrCreateUser(any(AuthDTO.class));
    }
}
