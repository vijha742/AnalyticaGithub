package com.vikas.service.impl;

import com.vikas.dto.CompResults;
import com.vikas.dto.GitHubUserResponse;
import com.vikas.dto.UserComparisonDTO;
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
import java.util.*;

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

    @Value("${github.api.graphql-url}")
    private String githubGraphqlUrl;

    @Value("${github.api.token}")
    private String githubToken;

    @Transactional
    @Override
    public SuggestedUser suggestUser(String githubUsername, String team) {
        User authenticatedUser =
                (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (suggestedUserRepository.existsByGithubUsernameAndSuggestedByAndTeam(
                githubUsername, authenticatedUser, team)) {
            SuggestedUser user =
                    suggestedUserRepository.findByGithubUsernameAndSuggestedByAndTeam(
                            githubUsername, authenticatedUser, team);
            user.setActive(true);
            return suggestedUserRepository.save(user);
        }
        var githubUser =
                addUserData(githubUsername)
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Failed to fetch user data from GitHub"));
        SuggestedUser suggestedUser = new SuggestedUser();
        suggestedUser.setGithubUsername(githubUsername);
        User suggestedBy = new User();
        suggestedBy.setId(authenticatedUser.getId());
        suggestedUser.setSuggestedBy(suggestedBy);
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
        suggestedUser.setTeam(team);
        suggestedUser.setLastRefreshed(Instant.now());
        // TODO: fetch the user's contributionCalendar
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

    @Override
    @Transactional
    public SuggestedUser refreshUserData(String githubUsername, String team) {
        // TODO: Error and Exception Handling...
        SuggestedUser suggestedUser =
                suggestedUserRepository.findByGithubUsernameAndTeam(githubUsername, team);

        var githubUser =
                addUserData(githubUsername)
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Failed to fetch user data from GitHub"));

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
        suggestedUser.setTeam(team);
        suggestedUser.setLastRefreshed(Instant.now());

        return suggestedUserRepository.save(suggestedUser);
    }

    @Override
    public List<SuggestedUser> getAllActiveUsers(String team) {
        return suggestedUserRepository.findByActiveTrueAndSuggestedByAndTeam(
                (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), team);
    }

    public void cpmpareUser() {}

    //
    // @Override
    // public Optional<SuggestedUser> getUserByUsername(String githubUsername) {
    // return suggestedUserRepository.findByGithubUsername(githubUsername);
    // }
    //
    // public String getQueryBuilder(String githubUsername, int i) {
    // String filteredUser = githubUsername.replaceAll("[^a-zA-Z0-9]", "");
    // String data = filteredUser + ": user (login: \"" + githubUsername + "\") {\n"
    // +
    // " id\n" +
    // " login\n" +
    // " name\n" +
    // " email\n" +
    // " avatarUrl\n" +
    // " bio\n" +
    // " followers {\n" +
    // " totalCount\n" +
    // " }\n" +
    // " following {\n" +
    // " totalCount\n" +
    // " }\n" +
    // " repositories(first: 100) {\n" +
    // " totalCount\n" +
    // " nodes {\n" +
    // " id\n" +
    // " name\n" +
    // " description\n" +
    // " primaryLanguage {\n" +
    // " name\n" +
    // " }\n" +
    // " stargazerCount\n" +
    // " forkCount\n" +
    // " isPrivate\n" +
    // " createdAt\n" +
    // " updatedAt\n" +
    // " }\n" +
    // " }\n" +
    // " contributionsCollection {\n" +
    // " totalCommitContributions\n" +
    // " totalPullRequestContributions\n" +
    // " totalIssueContributions\n" +
    // " totalRepositoryContributions\n" +
    // " }\n" +
    // " }\n";
    // return data;
    // }
    //
    // // TODO: implemList<GithubUser> list = in a batch to get all users data from
    // the DB to reduce time...
    // // TODO: to reduce latency here what we can do is chop down the request size
    // into groups of 6 and send or groups of 3 with parallel operations on them(but
    // it'd consume up queries fastly.)
    // @Override
    // public List<SuggestedUser> getActiveSuggestedUsersWithTimeoutForUser(String
    // username) {
    // List<SuggestedUser> suggestedUsers =
    // suggestedUserRepository.findByActiveTrueAndSuggestedBy(username);
    // System.out.println(suggestedUsers);
    // long start = System.currentTimeMillis();
    // if (suggestedUsers.isEmpty()) {
    // return new ArrayList<>();
    // } else {
    // return suggestedUsers;
    // }
    // final int MAX_USERS_IN_SINGLE_QUERY = 6;
    // final int TIMEOUT_SECONDS = 30;
    // int totalUsers = suggestedUsers.size();
    // List<CompletableFuture<List<User>>> futures = new ArrayList<>();
    // for (int startIndex = 0; startIndex < totalUsers; startIndex +=
    // MAX_USERS_IN_SINGLE_QUERY) {
    // int endIndex = Math.min(startIndex + MAX_USERS_IN_SINGLE_QUERY, totalUsers);
    // final int batchStart = startIndex;
    // final int batchEnd = endIndex;
    //
    // CompletableFuture<List<User>> future = CompletableFuture.supplyAsync(() ->
    // fetchBatch(suggestedUsers, batchStart, batchEnd))
    // .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
    // .exceptionally(ex -> {
    // log.error("Batch failed for indexes {}-{}", batchStart, batchEnd, ex);
    // return new ArrayList<>();
    // });
    // futures.add(future);
    // }
    //
    // try {
    // CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
    // .get(TIMEOUT_SECONDS + 10, TimeUnit.SECONDS);
    // List<User> allUsers = new ArrayList<>(totalUsers);
    // for (CompletableFuture<List<User>> future : futures) {
    // allUsers.addAll(future.get());
    // }
    // System.out.println("Time to complete request: " + (System.currentTimeMillis()
    // - start) + " ms");
    // return allUsers;
    // } catch (TimeoutException e) {
    // log.error("Timeout waiting for all batches to complete", e);
    // return collectPartialResults(futures);
    // } catch (Exception e) {
    // log.error("Error fetching suggested users", e);
    // throw new RuntimeException("Failed to fetch suggested users", e);
    // }
    // }
    //
    // private List<GithubUser> fetchBatch(List<SuggestedUser> suggestedUsers, int
    // startIndex, int endIndex) {
    // int batchSize = endIndex - startIndex;
    // List<GithubUser> batchResults = new ArrayList<>(batchSize);
    //
    // StringBuilder queryBuilder = new StringBuilder("query {");
    // for (int i = 0; i < batchSize; i++) {
    // String username = suggestedUsers.get(startIndex + i).getGithubUsername();
    // queryBuilder.append(getQueryBuilder(username, i + 1));
    // }
    // queryBuilder.append("}");
    //
    // try {
    // Map<String, Object> response =
    // githubClient.executeQuery(queryBuilder.toString(),
    // Collections.emptyMap());
    // if (response != null) {
    // for (Object userDataObj : response.values()) {
    // if (userDataObj instanceof Map) {
    // @SuppressWarnings("unchecked")
    // Map<String, Object> userData = (Map<String, Object>) userDataObj;
    // GithubUser user = GithubUser.mapToUser(userData);
    // batchResults.add(user);
    // }
    // }
    // }
    // } catch (Exception e) {
    // log.error("Error fetching batch {}-{}", startIndex, endIndex, e);
    // }
    // return batchResults;
    // }
    //
    // private List<GithubUser>
    // collectPartialResults(List<CompletableFuture<List<GithubUser>>> futures) {
    // List<GithubUser> partialResults = new ArrayList<>();
    // for (CompletableFuture<List<GithubUser>> future : futures) {
    // if (future.isDone() && !future.isCompletedExceptionally()) {
    // try {
    // partialResults.addAll(future.get());
    // } catch (Exception e) {
    // log.warn("Could not get result from completed future", e);
    // }
    // }
    // }
    // return partialResults;
    // }
    //
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
    public UserComparisonDTO compareTwoUsers(String githubUsername1, String githubUsername2) {
        SuggestedUser user1 = suggestedUserRepository.findFirstByGithubUsername(githubUsername1);
        SuggestedUser user2 = suggestedUserRepository.findFirstByGithubUsername(githubUsername2);

        if (user1 == null || user2 == null) {
            throw new RuntimeException("One or both users not found in the specified team.");
        }

        CompResults results = new CompResults(user1, user2);

        return UserComparisonDTO.builder().user1(user1).user2(user2).results(results).build();
    }

    //
    // @Override
    // public boolean isUserSuggested(String githubUsername) {
    // return suggestedUserRepository.existsByGithubUsername(githubUsername);
    // }

    @Override
    @Transactional
    public List<SuggestedUser> getLeaderboard() {
        User authenticatedUser =
                (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return suggestedUserRepository.findTop10BySuggestedByAndOrderByTotalContributionsDesc(
                authenticatedUser);
    }
}
