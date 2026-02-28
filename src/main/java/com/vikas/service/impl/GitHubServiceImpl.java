package com.vikas.service.impl;

import com.vikas.dto.AuthDTO;
import com.vikas.dto.CompResults;
import com.vikas.dto.UserComparisonDTO;
import com.vikas.model.GithubRepository;
import com.vikas.model.TechTimeline;
import com.vikas.model.TechnicalProfile;
import com.vikas.model.User;
import com.vikas.repository.SuggestedUserRepoDataRepository;
import com.vikas.repository.SuggestedUserRepository;
import com.vikas.repository.UserRepository;
import com.vikas.service.AnalyticsService;
import com.vikas.service.GitHubService;
import com.vikas.service.RepositoryAnalyticsService;
import com.vikas.utils.GithubGraphQLClient;
import com.vikas.utils.MapUtils;
import com.vikas.utils.QueryManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GitHubServiceImpl implements GitHubService {

    @Value("${github.api.graphql-url}")
    private String githubGraphqlUrl;

    @Value("${github.api.token}")
    private String githubToken;

    private final UserRepository userRepository;
    private final RepositoryAnalyticsService repoService;
    private final SuggestedUserRepository suggestedUserRepo;
    private final SuggestedUserRepoDataRepository suggestedUserRepoRepository;
    private final AnalyticsService analyticsService;
    private final QueryManager queryHub;
    private final GithubGraphQLClient githubClient;

    @Transactional
    @Override
    public User findOrCreateUser(AuthDTO githubUser) {
        return userRepository
                .findByGithubUsername(githubUser.getUserName())
                .orElseGet(
                        () -> {
                            User newUser = new User();
                            newUser.setGithubUsername(githubUser.getUserName());
                            newUser.setName(githubUser.getName());
                            newUser.setEmail(githubUser.getEmail());
                            newUser.setAvatarUrl(githubUser.getAvatarUrl());
                            newUser.setBio(githubUser.getBio());
                            newUser.setFollowersCount(githubUser.getFollowersCount());
                            newUser.setFollowingCount(githubUser.getFollowingCount());
                            newUser.setPublicReposCount(githubUser.getPublicReposCount());
                            newUser.setCreatedAt(githubUser.getCreated_at());
                            newUser.setTeams(Arrays.asList("Classmates", "Friends", "Colleagues"));
                            // Contribution contri = analyticsService.getContributions(
                            // githubUser.getUserName(), "weekly");
                            // newUser.setTotalContributions(contri.getTotalContributions());
                            TechnicalProfile techAnalysis = new TechnicalProfile();
                            techAnalysis = repoService.getTechnicalProfile(githubUser.getUserName());
                            newUser.setTechnicalProfile(techAnalysis);
                            TechTimeline userTech = new TechTimeline(techAnalysis);
                            newUser.setUserTech(userTech);
                            newUser.setLastUpdated(Instant.now());
                            return userRepository.save(newUser);
                        });
    }

    @Transactional
    @Override
    public User findUser(String Username) {
        Optional<User> userData = userRepository.findByGithubUsername(Username);
        return userData.orElse(getUser(Username));
    }

    /**
     * Parses a GitHub user map and populates a User object with basic information.
     * Does not include repository details or technical profile.
     *
     * @param gitHubUser The map containing GitHub user data
     * @return A User object with populated basic information
     */
    private User parseUserFromMap(Map<String, Object> gitHubUser) {
        User user = new User();
        user.setGithubUsername(MapUtils.extractStringFromMap(gitHubUser, "login", null));
        user.setName(MapUtils.extractStringFromMap(gitHubUser, "name", null));
        user.setEmail(MapUtils.extractStringFromMap(gitHubUser, "email", null));
        user.setAvatarUrl(MapUtils.extractStringFromMap(gitHubUser, "avatarUrl", null));
        user.setBio(MapUtils.extractStringFromMap(gitHubUser, "bio", null));
        
        String updatedAt = MapUtils.extractStringFromMap(gitHubUser, "updatedAt", null);
        if (updatedAt != null) {
            user.setLastUpdated(Instant.parse(updatedAt));
        }
        
        user.setFollowersCount(MapUtils.extractIntFromNestedMap(gitHubUser, "followers", "totalCount", 0));
        user.setFollowingCount(MapUtils.extractIntFromNestedMap(gitHubUser, "following", "totalCount", 0));
        user.setPublicReposCount(MapUtils.extractIntFromNestedMap(gitHubUser, "repositories", "totalCount", 0));
        
        return user;
    }

    public User getUser(String Username) {
        User user = new User();
        try {
            Map<String, Object> response = githubClient.executeQuery(
                    queryHub.fetchUserData(), Map.of("username", Username));
            if (response != null && response.get("user") instanceof Map) {
                Map<String, Object> gitHubUser = (Map<String, Object>) response.get("user");
                user = parseUserFromMap(gitHubUser);
                
                List<GithubRepository> repos = new ArrayList<>();
                if (gitHubUser.get("repositories") instanceof Map) {
                    Map<String, Object> repositories = (Map<String, Object>) gitHubUser.get("repositories");
                    if (repositories.get("nodes") instanceof List) {
                        List<Map<String, Object>> repoNodes = (List<Map<String, Object>>) repositories.get("nodes");
                        for (Map<String, Object> map : repoNodes) {
                            GithubRepository repo = new GithubRepository();
                            repo.setName((String) map.get("name"));
                            repo.setDescription((String) map.get("description"));

                            String language = null;
                            if (map.get("primaryLanguage") instanceof Map) {
                                Map<String, Object> primaryLang = (Map<String, Object>) map.get("primaryLanguage");
                                Object lang = primaryLang.get("name");
                                if (lang instanceof String) {
                                    language = ((String) lang);
                                }
                            }
                            repo.setLanguage(language);
                            repo.setStargazerCount(
                                    ((Integer) map.get("stargazerCount")).intValue());
                            repo.setForkCount(((Integer) map.get("forkCount")).intValue());
                            repo.setCreatedAt(Instant.parse((String) map.get("createdAt")));
                            repo.setUpdatedAt(Instant.parse((String) map.get("updatedAt")));
                            repo.setIsPrivate(((Boolean) map.get("isPrivate")));
                            repos.add(repo);
                        }
                    }
                }
                user.setUserRepository(repos);
                if (gitHubUser.get("contributionsCollection") instanceof Map) {
                    Map<String, Object> contributionsCollection = (Map<String, Object>) gitHubUser
                            .get("contributionsCollection");
                    if (contributionsCollection != null
                            && contributionsCollection.get("contributionCalendar") instanceof Map) {
                        Map<String, Object> map = (Map<String, Object>) contributionsCollection
                                .get("contributionCalendar");
                        int totalContributions = ((Integer) map.get("totalContributions")).intValue();
                        user.setTotalContributions(totalContributions);
                    }
                }
                TechnicalProfile techAnalysis = new TechnicalProfile();
                techAnalysis = repoService.getTechnicalProfile(user.getGithubUsername());
                user.setTechnicalProfile(techAnalysis);
                TechTimeline userTech = new TechTimeline(techAnalysis);
                user.setUserTech(userTech);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public List<User> searchUsers(String query, int limit) {
        try {
            Map<String, Object> response = githubClient.executeQuery(
                    queryHub.searchUsers(), Map.of("query", query, "first", limit));
            if (response != null && response.get("search") instanceof Map) {
                Map<String, Object> searchMap = (Map<String, Object>) response.get("search");
                Object nodesObj = searchMap.get("nodes");
                if (nodesObj instanceof List) {
                    List<User> users = new ArrayList<>();
                    List<?> nodes = (List<?>) nodesObj;
                    for (Object nodeObj : nodes) {
                        if (nodeObj instanceof Map) {
                            Map<String, Object> gitHubUser = (Map<String, Object>) nodeObj;
                            User user = parseUserFromMap(gitHubUser);
                            users.add(user);
                        }
                    }
                    return users;
                }
            }

            return List.of();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByGithubUsername(username);
    }

    @Override
    public List<String> getTeams() {
        User authenticatedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = authenticatedUser.getGithubUsername();
        User user = userRepository
                .findByGithubUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return authenticatedUser.getTeams();
    }

    @Override
    public List<String> createTeam(String team) {
        User authenticatedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = authenticatedUser.getGithubUsername();
        User user = userRepository
                .findByGithubUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        user.getTeams().add(team);
        userRepository.save(user);
        return user.getTeams();
    }

    // HACK: I'm currently just brute forcing this method. There must be some better
    // way to
    // implement it, which I need to do later.
    @Override
    public List<String> deleteTeam(String team) {
        User authenticatedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = authenticatedUser.getGithubUsername();
        User user = userRepository
                .findByGithubUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        user.getTeams().remove(team);
        userRepository.save(user);
        suggestedUserRepoRepository.deleteAllByUserAndTeam(user, team);
        suggestedUserRepo.deleteAllByUserAndTeam(user, team);
        return user.getTeams();
    }

    @Override
    @Transactional
    public List<User> getLeaderboard() {
        return userRepository.findTop10ByOrderByTotalContributionsDesc();
    }

    @Override
    @Transactional
    public UserComparisonDTO compareTwoUsers(String githubUsername1, String githubUsername2) {
        User user1 = getUser(githubUsername1);
        User user2 = getUser(githubUsername2);

        if (user1 == null || user2 == null) {
            throw new RuntimeException("One or both users not found in the specified team.");
        }

        CompResults results = new CompResults(user1, user2);

        return UserComparisonDTO.builder().user1(user1).user2(user2).results(results).build();
    }
}
