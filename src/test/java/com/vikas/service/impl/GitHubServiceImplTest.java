package com.vikas.service.impl;

import com.vikas.model.User;
import com.vikas.repository.SuggestedUserRepoDataRepository;
import com.vikas.repository.SuggestedUserRepository;
import com.vikas.repository.UserRepository;
import com.vikas.service.AnalyticsService;
import com.vikas.service.RepositoryAnalyticsService;
import com.vikas.utils.GithubGraphQLClient;
import com.vikas.utils.QueryManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitHubServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RepositoryAnalyticsService repoService;

    @Mock
    private SuggestedUserRepository suggestedUserRepo;

    @Mock
    private SuggestedUserRepoDataRepository suggestedUserRepoRepository;

    @Mock
    private AnalyticsService analyticsService;

    @Mock
    private QueryManager queryHub;

    @Mock
    private GithubGraphQLClient githubClient;

    @InjectMocks
    private GitHubServiceImpl gitHubService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setGithubUsername("testuser");
        testUser.setGithubId(123456L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setTeams(new ArrayList<>(Arrays.asList("Team1", "Team2", "Team3")));

        // Set up security context
        SecurityContext securityContext = mock(SecurityContext.class);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser, null, null);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void deleteTeam_shouldRollbackOnException() {
        // Arrange
        String teamToDelete = "Team2";
        when(userRepository.findByGithubUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Simulate an exception during the second delete operation
        doNothing().when(suggestedUserRepoRepository).deleteAllByUserAndTeam(eq(testUser), eq(teamToDelete));
        doThrow(new RuntimeException("Database connection failed"))
                .when(suggestedUserRepo).deleteAllByUserAndTeam(eq(testUser), eq(teamToDelete));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            gitHubService.deleteTeam(teamToDelete);
        });

        // Verify that the transaction should rollback
        // In a real integration test with @Transactional, the team would still be in the list
        verify(userRepository).findByGithubUsername("testuser");
        verify(userRepository).save(any(User.class));
        verify(suggestedUserRepoRepository).deleteAllByUserAndTeam(eq(testUser), eq(teamToDelete));
        verify(suggestedUserRepo).deleteAllByUserAndTeam(eq(testUser), eq(teamToDelete));
    }

    @Test
    void deleteTeam_shouldDeleteTeamSuccessfully() {
        // Arrange
        String teamToDelete = "Team2";
        when(userRepository.findByGithubUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(suggestedUserRepoRepository).deleteAllByUserAndTeam(eq(testUser), eq(teamToDelete));
        doNothing().when(suggestedUserRepo).deleteAllByUserAndTeam(eq(testUser), eq(teamToDelete));

        // Act
        List<String> remainingTeams = gitHubService.deleteTeam(teamToDelete);

        // Assert
        assertNotNull(remainingTeams);
        assertEquals(2, remainingTeams.size());
        assertFalse(remainingTeams.contains(teamToDelete));
        assertTrue(remainingTeams.contains("Team1"));
        assertTrue(remainingTeams.contains("Team3"));

        verify(userRepository).findByGithubUsername("testuser");
        verify(userRepository).save(any(User.class));
        verify(suggestedUserRepoRepository).deleteAllByUserAndTeam(eq(testUser), eq(teamToDelete));
        verify(suggestedUserRepo).deleteAllByUserAndTeam(eq(testUser), eq(teamToDelete));
    }

    @Test
    void deleteTeam_shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        String teamToDelete = "Team2";
        when(userRepository.findByGithubUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gitHubService.deleteTeam(teamToDelete);
        });

        assertEquals("User not found: testuser", exception.getMessage());
        verify(userRepository).findByGithubUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
        verify(suggestedUserRepoRepository, never()).deleteAllByUserAndTeam(any(), any());
        verify(suggestedUserRepo, never()).deleteAllByUserAndTeam(any(), any());
    }

    @Test
    void deleteTeam_shouldEnsureAtomicOperation() {
        // Arrange
        String teamToDelete = "Team2";
        when(userRepository.findByGithubUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // Simulate failure at the save operation
        when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException("Save failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            gitHubService.deleteTeam(teamToDelete);
        });

        // Verify that the subsequent delete operations are not called
        verify(userRepository).findByGithubUsername("testuser");
        verify(userRepository).save(any(User.class));
        verify(suggestedUserRepoRepository, never()).deleteAllByUserAndTeam(any(), any());
        verify(suggestedUserRepo, never()).deleteAllByUserAndTeam(any(), any());
    }
}
