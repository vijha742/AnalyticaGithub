package com.vikas.service.impl;

import com.vikas.dto.GitHubUserResponse;
import com.vikas.dto.MatchedPeerDTO;
import com.vikas.model.SuggestedGithubRepository;
import com.vikas.model.SuggestedUser;
import com.vikas.model.User;
import com.vikas.repository.SuggestedUserRepoDataRepository;
import com.vikas.repository.SuggestedUserRepository;
import com.vikas.repository.UserRepository;
import com.vikas.service.SuggestedUserService;
import com.vikas.utils.GithubGraphQLClient;
import com.vikas.utils.QueryManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SuggestedUserServiceImpl implements SuggestedUserService {
    private final SuggestedUserRepository suggestedUserRepository;
    private final UserRepository userRepository;
    private final GithubGraphQLClient githubClient;
    private final QueryManager queryHub;
    private final RestTemplate restTemplate;
    private final SuggestedUserRepoDataRepository repoRepository;
    private final PeerMatchingService peerMatcher;

    @Value("${github.api.graphql-url}")
    private String githubGraphqlUrl;

    @Value("${github.api.token}")
    private String githubToken;

    @Transactional
    @Override
    public SuggestedUser suggestUser(String githubUsername, String team) {
        if (githubUsername == null || githubUsername.trim().isEmpty()) {
            throw new RuntimeException("GitHub username cannot be empty");
        }

        if (team == null || team.trim().isEmpty()) {
            throw new RuntimeException("Team cannot be empty");
        }
        User authenticatedUser =
                (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (suggestedUserRepository.existsByGithubUsernameAndSuggestedByAndTeam(
                githubUsername, authenticatedUser, team)) {
            SuggestedUser user =
                    suggestedUserRepository.findByGithubUsernameAndSuggestedByAndTeam(
                            githubUsername, authenticatedUser, team);
            if (user.isActive()) {
                throw new RuntimeException("User already Exists.");
            } else {
                user.setActive(true);
                return suggestedUserRepository.save(user);
            }
        }
        var githubUser =
                addUserData(githubUsername)
                        .orElseThrow(
                                () ->
                                         new RuntimeException(
                                                "Failed to fetch user data from GitHub"));
        SuggestedUser suggestedUser = new SuggestedUser();
        User suggestedBy = new User();
        suggestedBy.setId(authenticatedUser.getId());
        suggestedUser.setSuggestedBy(suggestedBy);
        
        populateSuggestedUserFromGithubData(suggestedUser, githubUser, githubUsername, team);
        
        List<SuggestedGithubRepository> repositories = new ArrayList<>();
        if (githubUser.getRepositories() != null
                && githubUser.getRepositories().getNodes() != null) {
            for (GitHubUserResponse.Repository repo : githubUser.getRepositories().getNodes()) {
                SuggestedGithubRepository mappedRepo = mapRepository(repo, suggestedUser);
                SuggestedGithubRepository createdRepo = repoRepository.save(mappedRepo);
                repositories.add(createdRepo);
            }
        }
        suggestedUser.setRepositories(repositories);
        return suggestedUserRepository.save(suggestedUser);
    }

    private Optional<GitHubUserResponse.User> addUserData(String githubUsername) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("username", githubUsername);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", queryHub.fetchUserData());
            requestBody.put("variables", variables);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + githubToken);
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            // TODO: Call for repo-analysis for tech and userReadme Analysis...
            // TODO: Search ways to get all these three queries concutrently
            GitHubUserResponse response =
                    restTemplate
                            .exchange(
                                    githubGraphqlUrl,
                                    HttpMethod.POST,
                                    entity,
                                    GitHubUserResponse.class)
                            .getBody();
            if (response != null
                    && response.getData() != null
                    && response.getData().getUser() != null) {
                GitHubUserResponse.ResponseData data = response.getData();
                GitHubUserResponse.User gitHubUser = data.getUser();
                // TODO: Need to change the query as well as change this
                // service method and
                // GithubUserResponse.class to incorporate the
                // totalContributions

                return Optional.of(gitHubUser);
            }

            return Optional.empty();

        } catch (Exception e) {
            log.error("Error fetching user data for {}: {}", githubUsername, e.getMessage());
            return Optional.empty();
        }
    }

    private SuggestedGithubRepository mapRepository(
            GitHubUserResponse.Repository repo, SuggestedUser user) {
        SuggestedGithubRepository mappedRepo = new SuggestedGithubRepository();
        mappedRepo.setName(repo.getName());
        mappedRepo.setDescription(repo.getDescription());
        mappedRepo.setLanguage(
                repo.getPrimaryLanguage() != null ? repo.getPrimaryLanguage().getName() : null);
        mappedRepo.setStargazerCount(repo.getStargazerCount());
        mappedRepo.setForkCount(repo.getForkCount());
        mappedRepo.setIsPrivate(repo.isPrivate());
        mappedRepo.setCreatedAt(Instant.parse(repo.getCreatedAt()));
        mappedRepo.setUpdatedAt(Instant.parse(repo.getUpdatedAt()));
        mappedRepo.setUser(user);
        return mappedRepo;
    }

    /**
     * Populates a SuggestedUser object with data from GitHub API response.
     *
     * @param suggestedUser The SuggestedUser object to populate
     * @param githubUser The GitHub API response data
     * @param githubUsername The GitHub username
     * @param team The team name
     */
    private void populateSuggestedUserFromGithubData(
            SuggestedUser suggestedUser,
            GitHubUserResponse.User githubUser,
            String githubUsername,
            String team) {
        suggestedUser.setGithubUsername(githubUsername);
        suggestedUser.setActive(true);
        suggestedUser.setName(githubUser.getName());
        suggestedUser.setEmail(githubUser.getEmail());
        suggestedUser.setAvatarUrl(githubUser.getAvatarUrl());
        suggestedUser.setBio(githubUser.getBio());
        suggestedUser.setFollowersCount(githubUser.getFollowers().getTotalCount());
        suggestedUser.setFollowingCount(githubUser.getFollowing().getTotalCount());
        suggestedUser.setPublicReposCount(githubUser.getRepositories().getTotalCount());
        suggestedUser.setPullRequestsCount(
                githubUser.getContributionsCollection() != null
                        ? githubUser.getContributionsCollection().getTotalPullRequestContributions()
                        : 0);
        suggestedUser.setIssuesCount(
                githubUser.getContributionsCollection() != null
                        ? githubUser.getContributionsCollection().getTotalIssueContributions()
                        : 0);
        suggestedUser.setCommitsCount(
                githubUser.getContributionsCollection() != null
                        ? githubUser.getContributionsCollection().getTotalCommitsCount()
                        : 0);
        suggestedUser.setTotalContributions(
                githubUser.getContributionsCollection() != null
                                && githubUser.getContributionsCollection().getTotalContributions()
                                        != null
                        ? githubUser
                                .getContributionsCollection()
                                .getTotalContributions()
                                .getTotalContributions()
                        : 0);
        suggestedUser.setTeam(team);
        suggestedUser.setLastRefreshed(Instant.now());
    }

    @Override
    @Transactional
    public SuggestedUser refreshUserData(String githubUsername, String team) {
        // TODO: Error and Exception Handling...
        User authenticatedUser =
                (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SuggestedUser suggestedUser =
                suggestedUserRepository.findByGithubUsernameAndSuggestedByAndTeam(
                        githubUsername, authenticatedUser, team);

        var githubUser =
                addUserData(githubUsername)
                        .orElseThrow(
                                () ->
                                         new RuntimeException(
                                                "Failed to fetch user data from GitHub"));

        populateSuggestedUserFromGithubData(suggestedUser, githubUser, githubUsername, team);
        
        List<SuggestedGithubRepository> repositories = suggestedUser.getRepositories();
        if (githubUser.getRepositories() != null
                && githubUser.getRepositories().getNodes() != null) {
            for (GitHubUserResponse.Repository repo : githubUser.getRepositories().getNodes()) {
                SuggestedGithubRepository mappedRepo = mapRepository(repo, suggestedUser);
                if (!repositories.contains(mappedRepo)) {
                    SuggestedGithubRepository createdRepo = repoRepository.save(mappedRepo);
                    repositories.add(createdRepo);
                }
            }
        }
        suggestedUser.setRepositories(repositories);

        return suggestedUserRepository.save(suggestedUser);
    }

    @Override
    public List<SuggestedUser> getAllActiveUsers(String team) {
        return suggestedUserRepository.findByActiveTrueAndSuggestedByAndTeam(
                (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), team);
    }

    @Override
    @Transactional
    public boolean deactivateUser(UUID id) {
        SuggestedUser user =
                suggestedUserRepository
                        .findById(id)
                         .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setActive(false);
        try {
            suggestedUserRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public List<SuggestedUser> getLeaderboard() {
        User authenticatedUser =
                (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return suggestedUserRepository
                .findTop10BySuggestedByAndActiveTrueOrderByTotalContributionsDesc(
                        authenticatedUser);
    }

    @Override
    @Transactional
    public List<MatchedPeerDTO> getCompMatch() {
        return new ArrayList<MatchedPeerDTO>();
    }

    @Override
    @Transactional
    public List<MatchedPeerDTO> getSuppMatch() {
        return peerMatcher.getSupplementingUser();
    }
}
