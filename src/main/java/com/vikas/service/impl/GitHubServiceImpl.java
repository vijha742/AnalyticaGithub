package com.vikas.service.impl;

import java.time.Instant;
import java.util.*;

import com.vikas.dto.AuthDTO;
import com.vikas.model.TechTimeline;
import com.vikas.model.TechnicalProfile;
import com.vikas.model.User;
import com.vikas.service.RepositoryAnalyticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.vikas.repository.UserRepository;
import com.vikas.service.GitHubService;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubServiceImpl implements GitHubService {

//    @Value("${github.api.graphql-url}")
//    private String githubGraphqlUrl;
//
//    @Value("${github.api.token}")
//    private String githubToken;

    private final UserRepository userRepository;
    private final RepositoryAnalyticsService repoService;

    @Override
    public User findOrCreateUser(AuthDTO githubUser) {
        return userRepository.findByGithubUsername(githubUser.getUserName())
                .orElseGet(() -> {
                    log.info("Creating new user for GitHub ID: {}", githubUser.getUserName());
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
                    System.out.println("Reached: Created at: " + githubUser.getCreated_at());
                    // newUser.setTotalContributions(githubUser.getTotalContributions());
                    TechnicalProfile techAnalysis = new TechnicalProfile();
                    techAnalysis = repoService.getTechnicalProfile(githubUser.getUserName());
                    TechTimeline userTech = new TechTimeline(techAnalysis);
                    newUser.setLastUpdated(Instant.now());
                    System.out.println("Reached: Last updated: " + newUser.getLastUpdated());
                    return userRepository.save(newUser);
                });
    }

    @Override
    public User findUser(String Username) {
        Optional<User> userData = userRepository.findByGithubUsername(Username);
        return userData.orElse(null);
    }

//    @Override
//    public Optional<User> addUserData(String githubUsername) {
//        try {
//            Map<String, Object> variables = new HashMap<>();
//            variables.put("username", githubUsername);
//
//            Map<String, Object> requestBody = new HashMap<>();
//            requestBody.put("query", queryHub.fetchUserData());
//            requestBody.put("variables", variables);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", "Bearer " + githubToken);
//            headers.set("Content-Type", "application/json");
//
//            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody,
//                    headers);
//            // TODO: Call for repo-analysis for tech and userReadme Analysis...
//            // TODO: Search ways to get all these three queries concutrently
//            GitHubUserResponse response = restTemplate.exchange(
//                    githubGraphqlUrl,
//                    HttpMethod.POST,
//                    entity,
//                    GitHubUserResponse.class).getBody();
//            User user = new User();
//            if (response != null && response.getData() != null &&
//                    response.getData().getUser() != null) {
//                GitHubUserResponse.ResponseData data = response.getData();
//                GitHubUserResponse.User gitHubUser = data.getUser();
//                user.setLastUpdated(Instant.now());
//                user.setGithubUsername(gitHubUser.getLogin());
//                user.setName(gitHubUser.getName());
//                user.setEmail(gitHubUser.getEmail());
//                user.setAvatarUrl(gitHubUser.getAvatarUrl());
//                user.setBio(gitHubUser.getBio());
//                user.setFollowersCount(gitHubUser.getFollowers().getTotalCount());
//                user.setFollowingCount(gitHubUser.getFollowing().getTotalCount());
//                user.setPublicReposCount(gitHubUser.getRepositories().getTotalCount());
//                // TODO: Need to change the query as well as change this service method and GithubUserResponse.class to incorporate the totalContributions
//                List<GithubRepository> repositories = new ArrayList<>();
//                if (gitHubUser.getRepositories() != null && gitHubUser.getRepositories().getNodes() != null) {
//                    for (GitHubUserResponse.Repository repo : gitHubUser.getRepositories().getNodes()) {
//                        GithubRepository mappedRepo = mapRepository(repo);
//                        repositories.add(mappedRepo);
//                    }
//                }
//                user.setRepositories(repositories);
//
//                List<Contribution> contributions = new ArrayList<>();
//                GitHubUserResponse.ContributionsCollection contributionsCollection = gitHubUser.getContributionsCollection();
//                if (contributionsCollection != null) {
//                    Contribution commitContribution = new Contribution();
//                    commitContribution.setId("commit_" + gitHubUser.getId());
//                    commitContribution.setDate(Instant.now());
//                    commitContribution.setCount(contributionsCollection.getTotalCommitContributions());
//                    commitContribution.setType("COMMIT");
//                    contributions.add(commitContribution);
//
//                    Contribution prContribution = new Contribution();
//                    prContribution.setId("pr_" + gitHubUser.getId());
//                    prContribution.setDate(Instant.now());
//                    prContribution.setCount(contributionsCollection.getTotalPullRequestContributions());
//                    prContribution.setType("PULL_REQUEST");
//                    contributions.add(prContribution);
//
//                    Contribution issueContribution = new Contribution();
//                    issueContribution.setId("issue_" + gitHubUser.getId());
//                    issueContribution.setDate(Instant.now());
//                    issueContribution.setCount(contributionsCollection.getTotalIssueContributions());
//                    issueContribution.setType("ISSUE");
//                    contributions.add(issueContribution);
//                }
//                user.setContributions(contributions);
//                return Optional.of(user);
//            }
//
//            return Optional.empty();
//        } catch (Exception e) {
//            log.error("Error fetching user data for {}: {}", githubUsername,
//                    e.getMessage(), e);
//            return null;
//        }
//    }

    //	public void updateUserData(User user) {
//		// TODO: Implement user data update logic using GraphQL mutations
//	}
//
//	@Override
//	public List<User> searchUsers(String query, int limit, int offset) {
//		try {
//			Map<String, Object> variables = new HashMap<>();
//			variables.put("query", query);
//			variables.put("first", limit);
//
//			Map<String, Object> requestBody = new HashMap<>();
//			requestBody.put("query", queryHub.searchUsers());
//			requestBody.put("variables", variables);
//
//			HttpHeaders headers = new HttpHeaders();
//			headers.set("Authorization", "Bearer " + githubToken);
//			headers.set("Content-Type", "application/json");
//
//			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody,
//					headers);
//
//			GitHubSearchResponse response = restTemplate.exchange(
//					githubGraphqlUrl,
//					HttpMethod.POST,
//					entity,
//					GitHubSearchResponse.class).getBody();
//
//			if (response != null && response.getData() != null &&
//					response.getData().getSearch() != null) {
//				List<User> users = new ArrayList<>();
//				for (GitHubSearchResponse.User gitHubUser : response.getData().getSearch().getNodes()) {
//					User user = new User();
//					user.setId(gitHubUser.getId());
//					user.setGithubUsername(gitHubUser.getLogin());
//					user.setName(gitHubUser.getName());
//					user.setEmail(gitHubUser.getEmail());
//					user.setAvatarUrl(gitHubUser.getAvatarUrl());
//					user.setBio(gitHubUser.getBio());
//					user.setFollowersCount(gitHubUser.getFollowers().getTotalCount());
//					user.setFollowingCount(gitHubUser.getFollowing().getTotalCount());
//					user.setPublicReposCount(gitHubUser.getRepositories().getTotalCount());
//
//					// Map repositories
//					List<GithubRepository> repositories = new ArrayList<>();
//					if (gitHubUser.getRepositories() != null &&
//							gitHubUser.getRepositories().getNodes() != null) {
//						for (GitHubSearchResponse.Repository repo : gitHubUser.getRepositories()
//								.getNodes()) {
//							repositories.add(mapRepository(repo));
//						}
//					}
//					user.setRepositories(repositories);
//
//					users.add(user);
//				}
//				return users;
//			}
//
//			return List.of();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return List.of();
//		}
//	}
//
//	@Override
//	public User refreshUserData(String githubUsername) {
//		User userData = findOrCreateUser(githubUsername);
//		if (userData != null) {
//			return userData;
//		}
//		throw new RuntimeException("Failed to refresh user data for: " +
//				githubUsername);
//	}
//
//	@Override
//	@SuppressWarnings("unchecked")
//	public ContributionCalendar getContributionTimeSeries(String username) {
//		Map<String, String> variables = Map.of("username", username);
//		Map<String, Object> response = githubClient.executeQuery(queryHub.getContributionCalendar(), variables);
//
//		if (response == null) {
//			return new ContributionCalendar();
//		}
//
//		try {
//			Map<String, Object> user = (Map<String, Object>) response.get("user");
//			if (user == null) {
//				return new ContributionCalendar();
//			}
//
//			Map<String, Object> contributionsCollection = (Map<String, Object>) user
//					.get("contributionsCollection");
//			if (contributionsCollection == null) {
//				return new ContributionCalendar();
//			}
//
//			Map<String, Object> contributionCalendar = (Map<String, Object>) contributionsCollection
//					.get("contributionCalendar");
//			if (contributionCalendar == null) {
//				return new ContributionCalendar();
//			}
//
//			Integer totalContributions = (Integer) contributionCalendar.get("totalContributions");
//			List<Map<String, Object>> weeksData = (List<Map<String, Object>>) contributionCalendar
//					.get("weeks");
//			List<ContributionWeek> weeks = new ArrayList<>();
//
//			if (weeksData != null) {
//				for (Map<String, Object> weekData : weeksData) {
//					String firstDay = (String) weekData.get("firstDay");
//					List<Map<String, Object>> contributionDaysData = (List<Map<String, Object>>) weekData
//							.get("contributionDays");
//					List<ContributionDay> contributionDays = new ArrayList<>();
//
//					if (contributionDaysData != null) {
//						for (Map<String, Object> dayData : contributionDaysData) {
//							String date = (String) dayData.get("date");
//							Integer contributionCount = (Integer) dayData
//									.get("contributionCount");
//
//							ContributionDay contributionDay = new ContributionDay();
//							contributionDay.setDate(date);
//							contributionDay.setContributionCount(
//									contributionCount != null ? contributionCount
//											: 0);
//							contributionDays.add(contributionDay);
//						}
//					}
//
//					if (!contributionDays.isEmpty()) {
//						ContributionWeek week = new ContributionWeek();
//						week.setFirstDay(firstDay);
//						week.setContributionDays(contributionDays);
//						weeks.add(week);
//					}
//				}
//			}
//
//			ContributionCalendar result = new ContributionCalendar();
//			result.setTotalContributions(totalContributions != null ? totalContributions : 0);
//			result.setWeeks(weeks);
//			return result;
//
//		} catch (ClassCastException | NullPointerException e) {
//			log.error("Error parsing GitHub API response for {}: {}", username,
//					e.getMessage());
//			return new ContributionCalendar();
//		}
//	}
//
//    private GithubRepository mapRepository(GitHubUserResponse.Repository repo) {
//        GithubRepository mappedRepo = new GithubRepository();
//        mappedRepo.setId(repo.getId());
//        mappedRepo.setName(repo.getName());
//        mappedRepo.setDescription(repo.getDescription());
//        mappedRepo.setLanguage(repo.getPrimaryLanguage() != null ? repo.getPrimaryLanguage().getName() : null);
//        mappedRepo.setStargazerCount(repo.getStargazerCount());
//        mappedRepo.setForkCount(repo.getForkCount());
//        mappedRepo.setIsPrivate(repo.isPrivate());
//        mappedRepo.setCreatedAt(Instant.parse(repo.getCreatedAt()));
//        mappedRepo.setUpdatedAt(Instant.parse(repo.getUpdatedAt()));
//
//        // Map topics
//        if (repo.getRepositoryTopics() != null &&
//                repo.getRepositoryTopics().getNodes() != null) {
//            mappedRepo.setTopics(repo.getRepositoryTopics().getNodes().stream()
//                    .map(node -> node.getTopic().getName())
//                    .collect(Collectors.toList()));
//        } else {
//            mappedRepo.setTopics(new ArrayList<>());
//        }
//
//        return mappedRepo;
//    }
//
//	private GithubRepository mapRepository(GitHubSearchResponse.Repository repo) {
//		GithubRepository mappedRepo = new GithubRepository();
//		mappedRepo.setId(repo.getId());
//		mappedRepo.setName(repo.getName());
//		mappedRepo.setDescription(repo.getDescription());
//		mappedRepo.setLanguage(repo.getPrimaryLanguage() != null ? repo.getPrimaryLanguage().getName() : null);
//		mappedRepo.setStargazerCount(repo.getStargazerCount());
//		mappedRepo.setForkCount(repo.getForkCount());
//		mappedRepo.setIsPrivate(repo.isPrivate());
//		mappedRepo.setCreatedAt(Instant.parse(repo.getCreatedAt()));
//		mappedRepo.setUpdatedAt(Instant.parse(repo.getUpdatedAt()));
//		mappedRepo.setTopics(new ArrayList<>());
//		return mappedRepo;
//	}

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByGithubUsername(username);
    }

}
