package com.vikas.service.impl;

import com.vikas.dto.AuthDTO;
import com.vikas.model.GithubRepository;
import com.vikas.model.TechTimeline;
import com.vikas.model.TechnicalProfile;
import com.vikas.model.User;
import com.vikas.repository.UserRepository;
import com.vikas.service.GitHubService;
import com.vikas.service.RepositoryAnalyticsService;
import com.vikas.utils.GithubGraphQLClient;
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
                            // newUser.setRole(Role.USER);
                            newUser.setFollowersCount(githubUser.getFollowersCount());
                            newUser.setFollowingCount(githubUser.getFollowingCount());
                            newUser.setPublicReposCount(githubUser.getPublicReposCount());
                            newUser.setCreatedAt(githubUser.getCreated_at());
                            newUser.setTeams(Arrays.asList("Classmates", "Friends", "Colleagues"));
                            // newUser.setTotalContributions(githubUser.getTotalContributions());
                            TechnicalProfile techAnalysis = new TechnicalProfile();
                            techAnalysis =
                                    repoService.getTechnicalProfile(githubUser.getUserName());
                            newUser.setTechnicalProfile(techAnalysis);
                            System.out.println(techAnalysis);
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

    public User getUser(String Username) {
        User user = new User();
        try {
            Map<String, Object> response =
                    githubClient.executeQuery(
                            queryHub.fetchUserData(), Map.of("username", Username));
            System.out.println("Query executed.." + response);
            if (response != null && response.get("user") instanceof Map) {
                Map<String, Object> gitHubUser = (Map<String, Object>) response.get("user");
                user.setGithubUsername((String) gitHubUser.get("login"));
                user.setName((String) gitHubUser.get("name"));
                user.setEmail((String) gitHubUser.get("email"));
                user.setAvatarUrl((String) gitHubUser.get("avatarUrl"));
                user.setBio((String) gitHubUser.get("bio"));
                user.setLastUpdated(Instant.parse((String) gitHubUser.get("updatedAt")));
                int followersCount = 0;
                if (gitHubUser.get("followers") instanceof Map) {
                    Map<String, Object> followers =
                            (Map<String, Object>) gitHubUser.get("followers");
                    Object totalFollowers = followers.get("totalCount");
                    if (totalFollowers instanceof Number) {
                        followersCount = ((Number) totalFollowers).intValue();
                    }
                }
                user.setFollowersCount(followersCount);
                int followingCount = 0;
                if (gitHubUser.get("following") instanceof Map) {
                    Map<String, Object> following =
                            (Map<String, Object>) gitHubUser.get("following");
                    Object totalFollowing = following.get("totalCount");
                    if (totalFollowing instanceof Number) {
                        followingCount = ((Number) totalFollowing).intValue();
                    }
                }
                user.setFollowingCount(followingCount);
                int publicReposCount = 0;
                List<GithubRepository> repos = new ArrayList<>();
                if (gitHubUser.get("repositories") instanceof Map) {
                    Map<String, Object> repositories =
                            (Map<String, Object>) gitHubUser.get("repositories");
                    Object totalRepos = repositories.get("totalCount");
                    if (totalRepos instanceof Number) {
                        publicReposCount = ((Number) totalRepos).intValue();
                    }
                    if (repositories.get("nodes") instanceof List) {
                        List<Map<String, Object>> repoNodes =
                                (List<Map<String, Object>>) repositories.get("nodes");
                        for (Map<String, Object> map : repoNodes) {
                            GithubRepository repo = new GithubRepository();
                            repo.setName((String) map.get("name"));
                            repo.setDescription((String) map.get("description"));

                            String language = null;
                            if (map.get("primaryLanguage") instanceof Map) {
                                Map<String, Object> primaryLang =
                                        (Map<String, Object>) map.get("primaryLanguage");
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
                user.setPublicReposCount(publicReposCount);
                if (gitHubUser.get("contributionsCollection") instanceof Map) {
                    Map<String, Object> contributionsCollection =
                            (Map<String, Object>) gitHubUser.get("contributionsCollection");
                    if (contributionsCollection != null
                            && contributionsCollection.get("contributionCalendar") instanceof Map) {
                        Map<String, Object> map =
                                (Map<String, Object>)
                                        contributionsCollection.get("contributionCalendar");
                        int totalContributions =
                                ((Integer) map.get("totalContributions")).intValue();
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

    // @Override
    // public Optional<User> addUserData(String githubUsername) {
    // try {
    // Map<String, Object> variables = new HashMap<>();
    // variables.put("username", githubUsername);
    //
    // Map<String, Object> requestBody = new HashMap<>();
    // requestBody.put("query", queryHub.fetchUserData());
    // requestBody.put("variables", variables);
    //
    // HttpHeaders headers = new HttpHeaders();
    // headers.set("Authorization", "Bearer " + githubToken);
    // headers.set("Content-Type", "application/json");
    //
    // HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody,
    // headers);
    // // TODO: Call for repo-analysis for tech and userReadme Analysis...
    // // TODO: Search ways to get all these three queries concutrently
    // GitHubUserResponse response = restTemplate.exchange(
    // githubGraphqlUrl,
    // HttpMethod.POST,
    // entity,
    // GitHubUserResponse.class).getBody();
    // User user = new User();
    // if (response != null && response.getData() != null &&
    // response.getData().getUser() != null) {
    // GitHubUserResponse.ResponseData data = response.getData();
    // GitHubUserResponse.User gitHubUser = data.getUser();
    // user.setLastUpdated(Instant.now());
    // user.setGithubUsername(gitHubUser.getLogin());
    // user.setName(gitHubUser.getName());
    // user.setEmail(gitHubUser.getEmail());
    // user.setAvatarUrl(gitHubUser.getAvatarUrl());
    // user.setBio(gitHubUser.getBio());
    // user.setFollowersCount(gitHubUser.getFollowers().getTotalCount());
    // user.setFollowingCount(gitHubUser.getFollowing().getTotalCount());
    // user.setPublicReposCount(gitHubUser.getRepositories().getTotalCount());
    // // TODO: Need to change the query as well as change this service method and
    // GithubUserResponse.class to incorporate the totalContributions
    // List<GithubRepository> repositories = new ArrayList<>();
    // if (gitHubUser.getRepositories() != null &&
    // gitHubUser.getRepositories().getNodes() != null) {
    // for (GitHubUserResponse.Repository repo :
    // gitHubUser.getRepositories().getNodes()) {
    // GithubRepository mappedRepo = mapRepository(repo);
    // repositories.add(mappedRepo);
    // }
    // }
    // user.setRepositories(repositories);
    //
    // List<Contribution> contributions = new ArrayList<>();
    // GitHubUserResponse.ContributionsCollection contributionsCollection =
    // gitHubUser.getContributionsCollection();
    // if (contributionsCollection != null) {
    // Contribution commitContribution = new Contribution();
    // commitContribution.setId("commit_" + gitHubUser.getId());
    // commitContribution.setDate(Instant.now());
    // commitContribution.setCount(contributionsCollection.getTotalCommitContributions());
    // commitContribution.setType("COMMIT");
    // contributions.add(commitContribution);
    //
    // Contribution prContribution = new Contribution();
    // prContribution.setId("pr_" + gitHubUser.getId());
    // prContribution.setDate(Instant.now());
    // prContribution.setCount(contributionsCollection.getTotalPullRequestContributions());
    // prContribution.setType("PULL_REQUEST");
    // contributions.add(prContribution);
    //
    // Contribution issueContribution = new Contribution();
    // issueContribution.setId("issue_" + gitHubUser.getId());
    // issueContribution.setDate(Instant.now());
    // issueContribution.setCount(contributionsCollection.getTotalIssueContributions());
    // issueContribution.setType("ISSUE");
    // contributions.add(issueContribution);
    // }
    // user.setContributions(contributions);
    // return Optional.of(user);
    // }
    //
    // return Optional.empty();
    // } catch (Exception e) {
    // log.error("Error fetching user data for {}: {}", githubUsername,
    // e.getMessage(), e);
    // return null;
    // }
    // }

    // public void updateUserData(User user) {
    // // TODO: Implement user data update logic using GraphQL mutations
    // }
    //
    @Override
    public List<User> searchUsers(String query, int limit) {
        try {
            Map<String, Object> response =
                    githubClient.executeQuery(
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
                            User user = new User();
                            user.setGithubUsername((String) gitHubUser.get("login"));
                            user.setName((String) gitHubUser.get("name"));
                            user.setEmail((String) gitHubUser.get("email"));
                            user.setAvatarUrl((String) gitHubUser.get("avatarUrl"));
                            user.setBio((String) gitHubUser.get("bio"));
                            int followersCount = 0;
                            if (gitHubUser.get("followers") instanceof Map) {
                                Map<String, Object> followers =
                                        (Map<String, Object>) gitHubUser.get("followers");
                                Object totalFollowers = followers.get("totalCount");
                                if (totalFollowers instanceof Number) {
                                    followersCount = ((Number) totalFollowers).intValue();
                                }
                            }
                            user.setFollowersCount(followersCount);
                            int followingCount = 0;
                            if (gitHubUser.get("following") instanceof Map) {
                                Map<String, Object> following =
                                        (Map<String, Object>) gitHubUser.get("following");
                                Object totalFollowing = following.get("totalCount");
                                if (totalFollowing instanceof Number) {
                                    followingCount = ((Number) totalFollowing).intValue();
                                }
                            }
                            user.setFollowingCount(followingCount);
                            int publicReposCount = 0;
                            if (gitHubUser.get("repositories") instanceof Map) {
                                Map<String, Object> repositories =
                                        (Map<String, Object>) gitHubUser.get("repositories");
                                Object totalRepos = repositories.get("totalCount");
                                if (totalRepos instanceof Number) {
                                    publicReposCount = ((Number) totalRepos).intValue();
                                }
                            }
                            user.setPublicReposCount(publicReposCount);
                            users.add(user);
                        }
                    }
                    System.out.println(users);
                    return users;
                }
            }

            return List.of();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // @Override
    // public User refreshUserData(String githubUsername) {
    // User userData = findOrCreateUser(githubUsername);
    // if (userData != null) {
    // return userData;
    // }
    // throw new RuntimeException("Failed to refresh user data for: " +
    // githubUsername);
    // }
    //
    // @Override
    // @SuppressWarnings("unchecked")
    // public ContributionCalendar getContributionTimeSeries(String username) {
    // Map<String, String> variables = Map.of("username", username);
    // Map<String, Object> response =
    // githubClient.executeQuery(queryHub.getContributionCalendar(), variables);
    //
    // if (response == null) {
    // return new ContributionCalendar();
    // }
    //
    // try {
    // Map<String, Object> user = (Map<String, Object>) response.get("user");
    // if (user == null) {
    // return new ContributionCalendar();
    // }
    //
    // Map<String, Object> contributionsCollection = (Map<String, Object>) user
    // .get("contributionsCollection");
    // if (contributionsCollection == null) {
    // return new ContributionCalendar();
    // }
    //
    // Map<String, Object> contributionCalendar = (Map<String, Object>)
    // contributionsCollection
    // .get("contributionCalendar");
    // if (contributionCalendar == null) {
    // return new ContributionCalendar();
    // }
    //
    // Integer totalContributions = (Integer)
    // contributionCalendar.get("totalContributions");
    // List<Map<String, Object>> weeksData = (List<Map<String, Object>>)
    // contributionCalendar
    // .get("weeks");
    // List<ContributionWeek> weeks = new ArrayList<>();
    //
    // if (weeksData != null) {
    // for (Map<String, Object> weekData : weeksData) {
    // String firstDay = (String) weekData.get("firstDay");
    // List<Map<String, Object>> contributionDaysData = (List<Map<String, Object>>)
    // weekData
    // .get("contributionDays");
    // List<ContributionDay> contributionDays = new ArrayList<>();
    //
    // if (contributionDaysData != null) {
    // for (Map<String, Object> dayData : contributionDaysData) {
    // String date = (String) dayData.get("date");
    // Integer contributionCount = (Integer) dayData
    // .get("contributionCount");
    //
    // ContributionDay contributionDay = new ContributionDay();
    // contributionDay.setDate(date);
    // contributionDay.setContributionCount(
    // contributionCount != null ? contributionCount
    // : 0);
    // contributionDays.add(contributionDay);
    // }
    // }
    //
    // if (!contributionDays.isEmpty()) {
    // ContributionWeek week = new ContributionWeek();
    // week.setFirstDay(firstDay);
    // week.setContributionDays(contributionDays);
    // weeks.add(week);
    // }
    // }
    // }
    //
    // ContributionCalendar result = new ContributionCalendar();
    // result.setTotalContributions(totalContributions != null ? totalContributions
    // : 0);
    // result.setWeeks(weeks);
    // return result;
    //
    // } catch (ClassCastException | NullPointerException e) {
    // log.error("Error parsing GitHub API response for {}: {}", username,
    // e.getMessage());
    // return new ContributionCalendar();
    // }
    // }
    //
    // private GithubRepository mapRepository(GitHubUserResponse.Repository repo) {
    // GithubRepository mappedRepo = new GithubRepository();
    // mappedRepo.setId(repo.getId());
    // mappedRepo.setName(repo.getName());
    // mappedRepo.setDescription(repo.getDescription());
    // mappedRepo.setLanguage(repo.getPrimaryLanguage() != null ?
    // repo.getPrimaryLanguage().getName() : null);
    // mappedRepo.setStargazerCount(repo.getStargazerCount());
    // mappedRepo.setForkCount(repo.getForkCount());
    // mappedRepo.setIsPrivate(repo.isPrivate());
    // mappedRepo.setCreatedAt(Instant.parse(repo.getCreatedAt()));
    // mappedRepo.setUpdatedAt(Instant.parse(repo.getUpdatedAt()));
    //
    // // Map topics
    // if (repo.getRepositoryTopics() != null &&
    // repo.getRepositoryTopics().getNodes() != null) {
    // mappedRepo.setTopics(repo.getRepositoryTopics().getNodes().stream()
    // .map(node -> node.getTopic().getName())
    // .collect(Collectors.toList()));
    // } else {
    // mappedRepo.setTopics(new ArrayList<>());
    // }
    //
    // return mappedRepo;
    // }
    //
    // private GithubRepository mapRepository(GitHubSearchResponse.Repository repo)
    // {
    // GithubRepository mappedRepo = new GithubRepository();
    // mappedRepo.setId(repo.getId());
    // mappedRepo.setName(repo.getName());
    // mappedRepo.setDescription(repo.getDescription());
    // mappedRepo.setLanguage(repo.getPrimaryLanguage() != null ?
    // repo.getPrimaryLanguage().getName() : null);
    // mappedRepo.setStargazerCount(repo.getStargazerCount());
    // mappedRepo.setForkCount(repo.getForkCount());
    // mappedRepo.setIsPrivate(repo.isPrivate());
    // mappedRepo.setCreatedAt(Instant.parse(repo.getCreatedAt()));
    // mappedRepo.setUpdatedAt(Instant.parse(repo.getUpdatedAt()));
    // mappedRepo.setTopics(new ArrayList<>());
    // return mappedRepo;
    // }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByGithubUsername(username);
    }

    @Override
    public List<String> createTeam(String team) {
        User authenticatedUser =
                (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = authenticatedUser.getGithubUsername();
        User user =
                userRepository
                        .findByGithubUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found: " + username));
        user.getTeams().add(team);
        userRepository.save(user);
        return user.getTeams();
    }

    @Override
    @Transactional
    public List<User> getLeaderboard() {
        return userRepository.findTop10ByOrderByTotalContributionsDesc();
    }
}
