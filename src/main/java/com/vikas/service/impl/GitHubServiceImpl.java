package com.vikas.service.impl;

import com.vikas.model.GithubUser;
import com.vikas.model.timeseries.TimeFrame;
import com.vikas.service.GitHubService;
import com.vikas.dto.GitHubUserResponse;
import com.vikas.dto.GitHubSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GitHubServiceImpl implements GitHubService {

    @Value("${github.api.graphql-url}")
    private String githubGraphqlUrl;

    @Value("${github.api.token}")
    private String githubToken;

    private final RestTemplate restTemplate;
    private int remainingRateLimit = 5000;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public GitHubServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Optional<GithubUser> fetchUserData(String githubUsername) {
        try {
            String query = """
                query($username: String!) {
                    user(login: $username) {
                        id
                        login
                        name
                        email
                        avatarUrl
                        bio
                        followers {
                            totalCount
                        }
                        following {
                            totalCount
                        }
                        repositories(first: 100) {
                            totalCount
                            nodes {
                                id
                                name
                                description
                                primaryLanguage {
                                    name
                                }
                                stargazerCount
                                forkCount
                                isPrivate
                                createdAt
                                updatedAt
                            }
                        }
                        contributionsCollection {
                            totalCommitContributions
                            totalPullRequestContributions
                            totalIssueContributions
                            totalRepositoryContributions
                        }
                    }
                    rateLimit {
                        limit
                        remaining
                        resetAt
                    }
                }
                """;

            Map<String, Object> variables = new HashMap<>();
            variables.put("username", githubUsername);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", query);
            requestBody.put("variables", variables);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + githubToken);
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            GitHubUserResponse response = restTemplate.exchange(
                    githubGraphqlUrl,
                    HttpMethod.POST,
                    entity,
                    GitHubUserResponse.class
            ).getBody();

            if (response != null && response.getData() != null && response.getData().getUser() != null) {
                GitHubUserResponse.ResponseData data = response.getData();
                GitHubUserResponse.User gitHubUser = data.getUser();
                GitHubUserResponse.RateLimit rateLimit = data.getRateLimit();

                // Update rate limit
                remainingRateLimit = rateLimit.getRemaining();

                // Map GitHub user to our GithubUser entity
                GithubUser user = new GithubUser();
                user.setId(gitHubUser.getId());
                user.setGithubUsername(gitHubUser.getLogin());
                user.setName(gitHubUser.getName());
                user.setEmail(gitHubUser.getEmail());
                user.setAvatarUrl(gitHubUser.getAvatarUrl());
                user.setBio(gitHubUser.getBio());
                user.setFollowersCount(gitHubUser.getFollowers().getTotalCount());
                user.setFollowingCount(gitHubUser.getFollowing().getTotalCount());
                user.setPublicReposCount(gitHubUser.getRepositories().getTotalCount());

                // Map repositories
                List<GithubUser.Repository> repositories = new ArrayList<>();
                if (gitHubUser.getRepositories() != null && gitHubUser.getRepositories().getNodes() != null) {
                    for (GitHubUserResponse.Repository repo : gitHubUser.getRepositories().getNodes()) {
                        repositories.add(mapRepository(repo));
                    }
                }
                user.setRepositories(repositories);

                // Map contributions
                List<GithubUser.Contribution> contributions = new ArrayList<>();
                GitHubUserResponse.ContributionsCollection contributionsCollection = gitHubUser.getContributionsCollection();
                if (contributionsCollection != null) {
                    GithubUser.Contribution commitContribution = new GithubUser.Contribution();
                    commitContribution.setId("commit_" + gitHubUser.getId());
                    commitContribution.setDate(Instant.now());
                    commitContribution.setCount(contributionsCollection.getTotalCommitContributions());
                    commitContribution.setType("COMMIT");
                    contributions.add(commitContribution);

                    GithubUser.Contribution prContribution = new GithubUser.Contribution();
                    prContribution.setId("pr_" + gitHubUser.getId());
                    prContribution.setDate(Instant.now());
                    prContribution.setCount(contributionsCollection.getTotalPullRequestContributions());
                    prContribution.setType("PULL_REQUEST");
                    contributions.add(prContribution);

                    GithubUser.Contribution issueContribution = new GithubUser.Contribution();
                    issueContribution.setId("issue_" + gitHubUser.getId());
                    issueContribution.setDate(Instant.now());
                    issueContribution.setCount(contributionsCollection.getTotalIssueContributions());
                    issueContribution.setType("ISSUE");
                    contributions.add(issueContribution);
                }
                user.setContributions(contributions);

                return Optional.of(user);
            }

            return Optional.empty();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public void updateUserData(GithubUser user) {
        // TODO: Implement user data update logic using GraphQL mutations
    }

    @Override
    public boolean isRateLimitExceeded() {
        return remainingRateLimit <= 0;
    }

    @Override
    public int getRemainingRateLimit() {
        return remainingRateLimit;
    }

    @Override
    public List<GithubUser> searchUsers(String query, int limit, int offset) {
        try {
            String searchQuery = """
                query($query: String!, $first: Int!, $after: String) {
                    search(query: $query, type: USER, first: $first, after: $after) {
                        nodes {
                            ... on User {
                                id
                                login
                                name
                                email
                                avatarUrl
                                bio
                                followers {
                                    totalCount
                                }
                                following {
                                    totalCount
                                }
                                repositories(first: 100) {
                                    totalCount
                                    nodes {
                                        id
                                        name
                                        description
                                        primaryLanguage {
                                            name
                                        }
                                        stargazerCount
                                        forkCount
                                        isPrivate
                                        createdAt
                                        updatedAt
                                    }
                                }
                            }
                        }
                    }
                }
                """;

            Map<String, Object> variables = new HashMap<>();
            variables.put("query", query);
            variables.put("first", limit);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", searchQuery);
            requestBody.put("variables", variables);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + githubToken);
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            GitHubSearchResponse response = restTemplate.exchange(
                    githubGraphqlUrl,
                    HttpMethod.POST,
                    entity,
                    GitHubSearchResponse.class
            ).getBody();

            if (response != null && response.getData() != null && response.getData().getSearch() != null) {
                List<GithubUser> users = new ArrayList<>();
                for (GitHubSearchResponse.User gitHubUser : response.getData().getSearch().getNodes()) {
                    GithubUser user = new GithubUser();
                    user.setId(gitHubUser.getId());
                    user.setGithubUsername(gitHubUser.getLogin());
                    user.setName(gitHubUser.getName());
                    user.setEmail(gitHubUser.getEmail());
                    user.setAvatarUrl(gitHubUser.getAvatarUrl());
                    user.setBio(gitHubUser.getBio());
                    user.setFollowersCount(gitHubUser.getFollowers().getTotalCount());
                    user.setFollowingCount(gitHubUser.getFollowing().getTotalCount());
                    user.setPublicReposCount(gitHubUser.getRepositories().getTotalCount());

                    // Map repositories
                    List<GithubUser.Repository> repositories = new ArrayList<>();
                    if (gitHubUser.getRepositories() != null && gitHubUser.getRepositories().getNodes() != null) {
                        for (GitHubSearchResponse.Repository repo : gitHubUser.getRepositories().getNodes()) {
                            repositories.add(mapRepository(repo));
                        }
                    }
                    user.setRepositories(repositories);

                    users.add(user);
                }
                return users;
            }

            return List.of();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public boolean verifyUserExists(String githubUsername) {
        try {
            String query = """
                query($username: String!) {
                    user(login: $username) {
                        id
                    }
                }
                """;

            Map<String, Object> variables = new HashMap<>();
            variables.put("username", githubUsername);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", query);
            requestBody.put("variables", variables);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + githubToken);
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            GitHubUserResponse response = restTemplate.exchange(
                    githubGraphqlUrl,
                    HttpMethod.POST,
                    entity,
                    GitHubUserResponse.class
            ).getBody();

            return response != null && 
                   response.getData() != null && 
                   response.getData().getUser() != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public GithubUser refreshUserData(String githubUsername) {
        Optional<GithubUser> userData = fetchUserData(githubUsername);
        if (userData.isPresent()) {
            GithubUser user = userData.get();
            return user;
        }
        throw new RuntimeException("Failed to refresh user data for: " + githubUsername);
    }

    @Override
    public Map<LocalDate, Integer> getContributionTimeSeries(String username, String timeFrame, 
                                                            LocalDate startDate, LocalDate endDate, 
                                                            List<String> contributionTypes) {
        // Get the user's contributions
        Optional<GithubUser> userOptional = fetchUserData(username);
        if (userOptional.isEmpty()) {
            return Map.of(); // Return empty map if user not found
        }

        GithubUser user = userOptional.get();
        List<GithubUser.Contribution> contributions = user.getContributions();
        
        if (contributions == null || contributions.isEmpty()) {
            return Map.of(); // Return empty map if no contributions
        }

        // Filter by contribution types if specified
        List<GithubUser.Contribution> filteredContributions = contributions;
        if (contributionTypes != null && !contributionTypes.isEmpty()) {
            filteredContributions = contributions.stream()
                .filter(c -> contributionTypes.contains(c.getType()))
                .toList();
        }

        // Convert Instant dates to LocalDate
        Map<LocalDate, Integer> contributionsByDate = new HashMap<>();
        
        for (GithubUser.Contribution contribution : filteredContributions) {
            LocalDate date = contribution.getDate().atZone(java.time.ZoneOffset.UTC).toLocalDate();
            
            // Skip if outside requested date range
            if ((startDate != null && date.isBefore(startDate)) || 
                (endDate != null && date.isAfter(endDate))) {
                continue;
            }
            
            // Aggregate by the appropriate time frame
            LocalDate aggregationKey = switch(TimeFrame.valueOf(timeFrame.toUpperCase())) {
                case DAILY -> date;
                case WEEKLY -> date.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                case MONTHLY -> date.withDayOfMonth(1);
                case YEARLY -> date.withDayOfYear(1);
            };
            
            // Add to the map with aggregation
            contributionsByDate.merge(aggregationKey, contribution.getCount(), Integer::sum);
        }
        
        return contributionsByDate;
    }

    private GithubUser.Repository mapRepository(GitHubUserResponse.Repository repo) {
        GithubUser.Repository mappedRepo = new GithubUser.Repository();
        mappedRepo.setId(repo.getId());
        mappedRepo.setName(repo.getName());
        mappedRepo.setDescription(repo.getDescription());
        mappedRepo.setLanguage(repo.getPrimaryLanguage() != null ? repo.getPrimaryLanguage().getName() : null);
        mappedRepo.setStargazerCount(repo.getStargazerCount());
        mappedRepo.setForkCount(repo.getForkCount());
        mappedRepo.setIsPrivate(repo.isPrivate());
        mappedRepo.setCreatedAt(Instant.parse(repo.getCreatedAt()));
        mappedRepo.setUpdatedAt(Instant.parse(repo.getUpdatedAt()));
        return mappedRepo;
    }

    private GithubUser.Repository mapRepository(GitHubSearchResponse.Repository repo) {
        GithubUser.Repository mappedRepo = new GithubUser.Repository();
        mappedRepo.setId(repo.getId());
        mappedRepo.setName(repo.getName());
        mappedRepo.setDescription(repo.getDescription());
        mappedRepo.setLanguage(repo.getPrimaryLanguage() != null ? repo.getPrimaryLanguage().getName() : null);
        mappedRepo.setStargazerCount(repo.getStargazerCount());
        mappedRepo.setForkCount(repo.getForkCount());
        mappedRepo.setIsPrivate(repo.isPrivate());
        mappedRepo.setCreatedAt(Instant.parse(repo.getCreatedAt()));
        mappedRepo.setUpdatedAt(Instant.parse(repo.getUpdatedAt()));
        return mappedRepo;
    }
}