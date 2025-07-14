package com.vikas.service.impl;

import lombok.extern.slf4j.Slf4j;
import com.vikas.model.SuggestedUser;
import com.vikas.model.GithubUser;
import com.vikas.repository.SuggestedUserRepository;
import com.vikas.service.SuggestedUserService;
import com.vikas.service.GitHubService;
import com.vikas.utils.GithubGraphQLClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class SuggestedUserServiceImpl implements SuggestedUserService {
    private final SuggestedUserRepository suggestedUserRepository;
    private final GitHubService gitHubService;
    private final GithubGraphQLClient githubClient;

    public SuggestedUserServiceImpl(SuggestedUserRepository suggestedUserRepository, GitHubService gitHubService,
            GithubGraphQLClient githubClient) {
        this.suggestedUserRepository = suggestedUserRepository;
        this.gitHubService = gitHubService;
        this.githubClient = githubClient;
    }

    @Override
    @Transactional
    public SuggestedUser suggestUser(String githubUsername, String suggestedBy) {
        if (!githubClient.verifyUserExists(githubUsername)) {
            throw new RuntimeException("GitHub user does not exist: " + githubUsername);
        }
        if (suggestedUserRepository.existsByGithubUsername(githubUsername)) {
            throw new RuntimeException("User has already been suggested: " + githubUsername);
        }
        var githubUser = gitHubService.fetchUserData(githubUsername)
                .orElseThrow(() -> new RuntimeException("Failed to fetch user data from GitHub"));
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

    public String getQueryBuilder(String githubUsername, int i) {
        String filteredUser = githubUsername.replaceAll("[^a-zA-Z0-9]", "");
        String data = filteredUser + ": user (login: \"" + githubUsername + "\") {\n" +
                "                          id\n" +
                "                          login\n" +
                "                          name\n" +
                "                          email\n" +
                "                          avatarUrl\n" +
                "                          bio\n" +
                "                          followers {\n" +
                "                              totalCount\n" +
                "                          }\n" +
                "                          following {\n" +
                "                              totalCount\n" +
                "                          }\n" +
                "                          repositories(first: 100) {\n" +
                "                              totalCount\n" +
                "                              nodes {\n" +
                "                                  id\n" +
                "                                  name\n" +
                "                                  description\n" +
                "                                  primaryLanguage {\n" +
                "                                      name\n" +
                "                                  }\n" +
                "                                  stargazerCount\n" +
                "                                  forkCount\n" +
                "                                  isPrivate\n" +
                "                                  createdAt\n" +
                "                                  updatedAt\n" +
                "                              }\n" +
                "                          }\n" +
                "                          contributionsCollection {\n" +
                "                              totalCommitContributions\n" +
                "                              totalPullRequestContributions\n" +
                "                              totalIssueContributions\n" +
                "                              totalRepositoryContributions\n" +
                "                          }\n" +
                "                      }\n";
        return data;
    }

    // TODO: implemList<GithubUser> list = in a batch to get all users data from the
    // DB to reduce time...
    // TODO: to reduce latency here what we can do is chop down the request size
    // into groups of 6 and send or groups of 3 with parallel operations on them(but
    // it'd consume up queries fastly.)
    @Override
    public List<SuggestedUser> getActiveSuggestedUsersWithTimeoutForUser(String username) {
        List<SuggestedUser> suggestedUsers = suggestedUserRepository.findByActiveTrueAndSuggestedBy(username);
        System.out.println(suggestedUsers);
        long start = System.currentTimeMillis();
        if (suggestedUsers.isEmpty()) {
            return new ArrayList<>();
        } else {
            return suggestedUsers;
        }

//        final int MAX_USERS_IN_SINGLE_QUERY = 6;
//        final int TIMEOUT_SECONDS = 30;
//        int totalUsers = suggestedUsers.size();
//        List<CompletableFuture<List<GithubUser>>> futures = new ArrayList<>();
//        for (int startIndex = 0; startIndex < totalUsers; startIndex += MAX_USERS_IN_SINGLE_QUERY) {
//            int endIndex = Math.min(startIndex + MAX_USERS_IN_SINGLE_QUERY, totalUsers);
//            final int batchStart = startIndex;
//            final int batchEnd = endIndex;
//
//            CompletableFuture<List<GithubUser>> future = CompletableFuture
//                    .supplyAsync(() -> fetchBatch(suggestedUsers, batchStart, batchEnd))
//                    .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
//                    .exceptionally(ex -> {
//                        log.error("Batch failed for indexes {}-{}", batchStart, batchEnd, ex);
//                        return new ArrayList<>();
//                    });
//            futures.add(future);
//        }
//
//        try {
//            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
//                    .get(TIMEOUT_SECONDS + 10, TimeUnit.SECONDS);
//            List<GithubUser> allUsers = new ArrayList<>(totalUsers);
//            for (CompletableFuture<List<GithubUser>> future : futures) {
//                allUsers.addAll(future.get());
//            }
//            System.out.println("Time to complete request: " + (System.currentTimeMillis() - start) + " ms");
//            return allUsers;
//        } catch (TimeoutException e) {
//            log.error("Timeout waiting for all batches to complete", e);
//            return collectPartialResults(futures);
//        } catch (Exception e) {
//            log.error("Error fetching suggested users", e);
//            throw new RuntimeException("Failed to fetch suggested users", e);
//        }
    }

    private List<GithubUser> fetchBatch(List<SuggestedUser> suggestedUsers, int startIndex, int endIndex) {
        int batchSize = endIndex - startIndex;
        List<GithubUser> batchResults = new ArrayList<>(batchSize);

        StringBuilder queryBuilder = new StringBuilder("query {");
        for (int i = 0; i < batchSize; i++) {
            String username = suggestedUsers.get(startIndex + i).getGithubUsername();
            queryBuilder.append(getQueryBuilder(username, i + 1));
        }
        queryBuilder.append("}");

        try {
            Map<String, Object> response = githubClient.executeQuery(queryBuilder.toString(), Collections.emptyMap());
            if (response != null) {
                for (Object userDataObj : response.values()) {
                    if (userDataObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> userData = (Map<String, Object>) userDataObj;
                        GithubUser user = GithubUser.mapToUser(userData);
                        batchResults.add(user);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching batch {}-{}", startIndex, endIndex, e);
        }
        return batchResults;
    }

    private List<GithubUser> collectPartialResults(List<CompletableFuture<List<GithubUser>>> futures) {
        List<GithubUser> partialResults = new ArrayList<>();
        for (CompletableFuture<List<GithubUser>> future : futures) {
            if (future.isDone() && !future.isCompletedExceptionally()) {
                try {
                    partialResults.addAll(future.get());
                } catch (Exception e) {
                    log.warn("Could not get result from completed future", e);
                }
            }
        }
        return partialResults;
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        SuggestedUser user = suggestedUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setActive(false); // Changed from setIsActive to setActive
        suggestedUserRepository.save(user);
    }

    @Override
    public boolean isUserSuggested(String githubUsername) {
        return suggestedUserRepository.existsByGithubUsername(githubUsername);
    }
}
