package com.vikas.service.impl;

import java.time.Instant;
import java.util.*;

import com.vikas.dto.AuthDTO;
import com.vikas.model.User;
import com.vikas.model.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.vikas.dto.GitHubSearchResponse;
import com.vikas.dto.GitHubUserResponse;
import com.vikas.model.Contribution;
import com.vikas.model.GithubUser;
import com.vikas.model.Repository;
import com.vikas.model.timeseries.ContributionCalendar;
import com.vikas.model.timeseries.ContributionDay;
import com.vikas.model.timeseries.ContributionWeek;
import com.vikas.repository.UserRepository;
import com.vikas.service.GitHubService;
import com.vikas.utils.GithubGraphQLClient;
import com.vikas.utils.QueryManager;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubServiceImpl implements GitHubService {

	@Value("${github.api.graphql-url}")
	private String githubGraphqlUrl;

	@Value("${github.api.token}")
	private String githubToken;

	private final GithubGraphQLClient githubClient;
	private final QueryManager queryHub;
	private final RestTemplate restTemplate;
	private final UserRepository userRepository;

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
					// newUser.setTotalContributions(githubUser.getTotalContributions());
					return userRepository.save(newUser);
				});
	}

	@Override
	public User findOrCreateUser(String Username) {
		Optional<User> userData = userRepository.findByGithubUsername(Username);
		if (userData.isPresent()) return userData.get();
		else return createUserData(Username);
	}


	public User createUserData(String githubUsername) {
		try {
			Map<String, Object> variables = new HashMap<>();
			variables.put("username", githubUsername);

			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("query", queryHub.fetchUserData());
			requestBody.put("variables", variables);

			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer " + githubToken);
			headers.set("Content-Type", "application/json");

			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody,
					headers);

			GitHubUserResponse response = restTemplate.exchange(
					githubGraphqlUrl,
					HttpMethod.POST,
					entity,
					GitHubUserResponse.class).getBody();

			if (response != null && response.getData() != null &&
					response.getData().getUser() != null) {
				GitHubUserResponse.ResponseData data = response.getData();
				GitHubUserResponse.User gitHubUser = data.getUser();

				User user = new User();
				user.setGithubUsername(gitHubUser.getLogin());
				user.setName(gitHubUser.getName());
				user.setEmail(gitHubUser.getEmail());
				user.setAvatarUrl(gitHubUser.getAvatarUrl());
				user.setBio(gitHubUser.getBio());
				user.setFollowersCount(gitHubUser.getFollowers().getTotalCount());
				user.setFollowingCount(gitHubUser.getFollowing().getTotalCount());
				user.setPublicReposCount(gitHubUser.getRepositories().getTotalCount());
				// TODO: Need to change the query as well as change this service method and GithubUserResponse.class to incorporate the totalContributions
				user.setLastUpdated(Instant.now());
				user.setTotalContributions(gitHubUser.getContributionsCollection().getTotalContributions().getTotalCommitsCount());
				return user;
			}

			return null;
		} catch (Exception e) {
			log.error("Error fetching user data for {}: {}", githubUsername,
					e.getMessage(), e);
			return null;
		}
	}

            Map<String, Object> contributionCalendar = (Map<String, Object>) contributionsCollection
                    .get("contributionCalendar");
            if (contributionCalendar == null) {
                return new ContributionCalendar();
            }

            Integer totalContributions = (Integer) contributionCalendar.get("totalContributions");
            List<Map<String, Object>> weeksData = (List<Map<String, Object>>) contributionCalendar.get("weeks");
            List<ContributionWeek> weeks = new ArrayList<>();

            if (weeksData != null) {
                for (Map<String, Object> weekData : weeksData) {
                    String firstDay = (String) weekData.get("firstDay");
                    List<Map<String, Object>> contributionDaysData = (List<Map<String, Object>>) weekData
                            .get("contributionDays");
                    List<ContributionDay> contributionDays = new ArrayList<>();

                    if (contributionDaysData != null) {
                        for (Map<String, Object> dayData : contributionDaysData) {
                            String date = (String) dayData.get("date");
                            Integer contributionCount = (Integer) dayData.get("contributionCount");

                            ContributionDay contributionDay = new ContributionDay();
                            contributionDay.setDate(date);
                            contributionDay.setContributionCount(contributionCount != null ? contributionCount : 0);
                            contributionDays.add(contributionDay);
                        }
                    }

                    if (!contributionDays.isEmpty()) {
                        ContributionWeek week = new ContributionWeek();
                        week.setFirstDay(firstDay);
                        week.setContributionDays(contributionDays);
                        weeks.add(week);
                    }
                }
            }

            ContributionCalendar result = new ContributionCalendar();
            result.setTotalContributions(totalContributions != null ? totalContributions : 0);
            result.setWeeks(weeks);
            return result;

        } catch (ClassCastException | NullPointerException e) {
            log.error("Error parsing GitHub API response for {}: {}", username, e.getMessage());
            return new ContributionCalendar();
        }
    }

    private Repository mapRepository(GitHubUserResponse.Repository repo) {
        Repository mappedRepo = new Repository();
        mappedRepo.setId(repo.getId());
        mappedRepo.setName(repo.getName());
        mappedRepo.setDescription(repo.getDescription());
        mappedRepo.setLanguage(repo.getPrimaryLanguage() != null ? repo.getPrimaryLanguage().getName() : null);
        mappedRepo.setStargazerCount(repo.getStargazerCount());
        mappedRepo.setForkCount(repo.getForkCount());
        mappedRepo.setIsPrivate(repo.isPrivate());
        mappedRepo.setCreatedAt(Instant.parse(repo.getCreatedAt()));
        mappedRepo.setUpdatedAt(Instant.parse(repo.getUpdatedAt()));

        // Map topics
        // if (repo.getRepositoryTopics() != null &&
        // repo.getRepositoryTopics().getNodes() != null) {
        // mappedRepo.setTopics(repo.getRepositoryTopics().getNodes().stream()
        // .map(node -> node.getTopic().getName())
        // .collect(Collectors.toList()));
        // } else {
        // mappedRepo.setTopics(new ArrayList<>());
        // }

        return mappedRepo;
    }

    private Repository mapRepository(GitHubSearchResponse.Repository repo) {
        Repository mappedRepo = new Repository();
        mappedRepo.setId(repo.getId());
        mappedRepo.setName(repo.getName());
        mappedRepo.setDescription(repo.getDescription());
        mappedRepo.setLanguage(repo.getPrimaryLanguage() != null ? repo.getPrimaryLanguage().getName() : null);
        mappedRepo.setStargazerCount(repo.getStargazerCount());
        mappedRepo.setForkCount(repo.getForkCount());
        mappedRepo.setIsPrivate(repo.isPrivate());
        mappedRepo.setCreatedAt(Instant.parse(repo.getCreatedAt()));
        mappedRepo.setUpdatedAt(Instant.parse(repo.getUpdatedAt()));
        // mappedRepo.setTopics(new ArrayList<>());
        return mappedRepo;
    }
    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByGithubUsername(username);
    }

}
