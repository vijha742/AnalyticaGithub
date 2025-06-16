package com.vikas.service.impl;

import com.vikas.model.SuggestedUser;
import com.vikas.model.GithubUser;
import com.vikas.repository.SuggestedUserRepository;
import com.vikas.service.SuggestedUserService;
import com.vikas.service.GitHubService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class SuggestedUserServiceImpl implements SuggestedUserService {
    private final SuggestedUserRepository suggestedUserRepository;
    private final GitHubService gitHubService;

    public SuggestedUserServiceImpl(SuggestedUserRepository suggestedUserRepository, GitHubService gitHubService) {
        this.suggestedUserRepository = suggestedUserRepository;
        this.gitHubService = gitHubService;
    }

    @Override
    @Transactional
    public SuggestedUser suggestUser(String githubUsername, String suggestedBy) {
        // First verify if the user exists on GitHub
        if (!gitHubService.verifyUserExists(githubUsername)) {
            throw new RuntimeException("GitHub user does not exist: " + githubUsername);
        }

        // Check if user is already suggested
        if (suggestedUserRepository.existsByGithubUsername(githubUsername)) {
            throw new RuntimeException("User has already been suggested: " + githubUsername);
        }

        // Fetch user data from GitHub
        var githubUser = gitHubService.fetchUserData(githubUsername)
            .orElseThrow(() -> new RuntimeException("Failed to fetch user data from GitHub"));

        // Create new suggested user
        SuggestedUser suggestedUser = new SuggestedUser();
        suggestedUser.setGithubUsername(githubUsername);
        suggestedUser.setSuggestedBy(suggestedBy);
        suggestedUser.setActive(true);
        suggestedUser.setName(githubUser.getName());
        suggestedUser.setEmail(githubUser.getEmail());
        suggestedUser.setAvatarUrl(githubUser.getAvatarUrl());
        suggestedUser.setBio(githubUser.getBio());
        suggestedUser.setFollowersCount(githubUser.getFollowersCount());
        suggestedUser.setFollowingCount(githubUser.getFollowingCount());
        suggestedUser.setPublicReposCount(githubUser.getPublicReposCount());
        suggestedUser.setTotalContributions(githubUser.getTotalContributions());
        suggestedUser.setRepositories(githubUser.getRepositories());
        suggestedUser.setContributions(githubUser.getContributions());
        suggestedUser.setLastRefreshed(Instant.now());

        return suggestedUserRepository.save(suggestedUser);
    }

    @Override
    @Transactional
    public SuggestedUser refreshUserData(String githubUsername) {
        SuggestedUser suggestedUser = suggestedUserRepository.findByGithubUsername(githubUsername)
            .orElseThrow(() -> new RuntimeException("Suggested user not found: " + githubUsername));

        // Fetch fresh data from GitHub
        var githubUser = gitHubService.fetchUserData(githubUsername)
            .orElseThrow(() -> new RuntimeException("Failed to fetch user data from GitHub"));

        // Update user data
        suggestedUser.setName(githubUser.getName());
        suggestedUser.setEmail(githubUser.getEmail());
        suggestedUser.setAvatarUrl(githubUser.getAvatarUrl());
        suggestedUser.setBio(githubUser.getBio());
        suggestedUser.setFollowersCount(githubUser.getFollowersCount());
        suggestedUser.setFollowingCount(githubUser.getFollowingCount());
        suggestedUser.setPublicReposCount(githubUser.getPublicReposCount());
        suggestedUser.setTotalContributions(githubUser.getTotalContributions());
        suggestedUser.setRepositories(githubUser.getRepositories());
        suggestedUser.setContributions(githubUser.getContributions());
        suggestedUser.setLastRefreshed(Instant.now());

        return suggestedUserRepository.save(suggestedUser);
    }

    @Override
    public List<SuggestedUser> getAllActiveUsers() {
        return suggestedUserRepository.findByActiveTrue();
    }

    @Override
    public Optional<SuggestedUser> getUserByUsername(String githubUsername) {
        return suggestedUserRepository.findByGithubUsername(githubUsername);
    }

    @Override
    public List<GithubUser> getActiveSuggestedUsers() {
        List<SuggestedUser> suggestedUsers = suggestedUserRepository.findByActiveTrue();
        return suggestedUsers.stream()
            .map(user -> gitHubService.fetchUserData(user.getGithubUsername()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        SuggestedUser user = suggestedUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setActive(false);  // Changed from setIsActive to setActive
        suggestedUserRepository.save(user);
    }

    @Override
    public boolean isUserSuggested(String githubUsername) {
        return suggestedUserRepository.existsByGithubUsername(githubUsername);
    }
}