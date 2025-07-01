package com.vikas.service.impl;

import com.vikas.model.Contribution;
import com.vikas.model.Repository;
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

@Service
public class SuggestedUserServiceImpl implements SuggestedUserService {
    private final SuggestedUserRepository suggestedUserRepository;
    private final GitHubService gitHubService;
    private final GithubGraphQLClient githubClient;

    public SuggestedUserServiceImpl(SuggestedUserRepository suggestedUserRepository, GitHubService gitHubService, GithubGraphQLClient githubClient) {
        this.suggestedUserRepository = suggestedUserRepository;
        this.gitHubService = gitHubService;
        this.githubClient = githubClient;
    }

    @Override
    @Transactional
    public SuggestedUser suggestUser(String githubUsername, String suggestedBy) {
        if (!gitHubService.verifyUserExists(githubUsername)) {
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
        String data =  filteredUser + ": user (login: \"" + githubUsername + "\") {\n" +
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
    @Override
    public List<GithubUser> getActiveSuggestedUsers() {
        List<SuggestedUser> suggestedUsers = suggestedUserRepository.findByActiveTrue();
        long start = System.currentTimeMillis();
        // TODO: implemList<GithubUser> list = in a batch to get all users data from the DB to reduce time...
        if(!suggestedUsers.isEmpty()) {
            StringBuilder mainQuery =  new StringBuilder("query {");
            for (int i = 0; i < suggestedUsers.size(); i++) {
                mainQuery.append(getQueryBuilder(suggestedUsers.get(i).getGithubUsername(), i + 1));
            }
            mainQuery.append("}");
            System.out.println("Time to query to build: " + (System.currentTimeMillis() - start) + " ms");
            long queryStart = System.currentTimeMillis();
            Map<String, Object> response = githubClient.executeQuery(String.valueOf(mainQuery), new HashMap<>());
            //TODO: to reduce latency here what we can do is chop down the request size into groups of 6 and send or groups of 3 with parallel operations on them(but it'd consume up queries fastly.)
            System.out.println((System.currentTimeMillis() - queryStart) + " ms");
            if (response != null) {
                List<GithubUser> suggestedUserList = new ArrayList<>();
                for (Map.Entry<String, Object> entry : response.entrySet()) {
                    String username = entry.getKey();
                    Object userDataObj = entry.getValue();
                    if (userDataObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> userData = (Map<String, Object>) userDataObj;
                        GithubUser user = new GithubUser();
                        user.setId((String) userData.get("id"));
                        user.setGithubUsername((String) userData.get("login"));
                        user.setName((String) userData.get("name"));
                        user.setEmail((String) userData.get("email"));
                        user.setAvatarUrl((String) userData.get("avatarUrl"));
                        user.setBio((String) userData.get("bio"));
                        Object followersObj = userData.get("followers");
                        if (followersObj instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> followers = (Map<String, Object>) followersObj;
                            Object totalCount = followers.get("totalCount");
                            user.setFollowersCount(totalCount instanceof Integer ? (Integer) totalCount : 0);
                        }
                        Object followingObj = userData.get("following");
                        if (followingObj instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> following = (Map<String, Object>) followingObj;
                            Object totalCount = following.get("totalCount");
                            user.setFollowingCount(totalCount instanceof Integer ? (Integer) totalCount : 0);
                        }
                        Object repositoriesObj = userData.get("repositories");
                        if (repositoriesObj instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> repositories = (Map<String, Object>) repositoriesObj;
                            Object totalCount = repositories.get("totalCount");
                            user.setPublicReposCount(totalCount instanceof Integer ? (Integer) totalCount : 0);
                        }
                        user.setLastUpdated(Instant.now());
                        List<Repository> repositories = new ArrayList<>();
                        if (repositoriesObj instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> repositoriesData = (Map<String, Object>) repositoriesObj;
                            Object nodesObj = repositoriesData.get("nodes");
                            if (nodesObj instanceof List) {
                                @SuppressWarnings("unchecked")
                                List<Object> nodes = (List<Object>) nodesObj;
                                for (Object nodeObj : nodes) {
                                    if (nodeObj instanceof Map) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> repoData = (Map<String, Object>) nodeObj;
                                        Repository mappedRepo = mapRepository(repoData);
                                        repositories.add(mappedRepo);
                                    }
                                }
                            }
                        }
                        user.setRepositories(repositories);
                        List<Contribution> contributions = new ArrayList<>();
                        Object contributionsObj = userData.get("contributionsCollection");
                        if (contributionsObj instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> contributionsCollection = (Map<String, Object>) contributionsObj;
                            Contribution commitContribution = new Contribution();
                            commitContribution.setId("commit_" + userData.get("id"));
                            commitContribution.setDate(Instant.now());
                            Object commitCount = contributionsCollection.get("totalCommitContributions");
                            commitContribution.setCount(commitCount instanceof Integer ? (Integer) commitCount : 0);
                            commitContribution.setType("COMMIT");
                            contributions.add(commitContribution);
                            Contribution prContribution = new Contribution();
                            prContribution.setId("pr_" + userData.get("id"));
                            prContribution.setDate(Instant.now());
                            Object prCount = contributionsCollection.get("totalPullRequestContributions");
                            prContribution.setCount(prCount instanceof Integer ? (Integer) prCount : 0);
                            prContribution.setType("PULL_REQUEST");
                            contributions.add(prContribution);
                            Contribution issueContribution = new Contribution();
                            issueContribution.setId("issue_" + userData.get("id"));
                            issueContribution.setDate(Instant.now());
                            Object issueCount = contributionsCollection.get("totalIssueContributions");
                            issueContribution.setCount(issueCount instanceof Integer ? (Integer) issueCount : 0);
                            issueContribution.setType("ISSUE");
                            contributions.add(issueContribution);
                        }
                        user.setContributions(contributions);
                        suggestedUserList.add(user);
                    }
                }
                System.out.println("total time to process: " + (System.currentTimeMillis() - start) + " ms");
                return suggestedUserList;
            }
        }
        return new ArrayList<GithubUser>();
    }

    private Repository mapRepository(Map<String, Object> repoData) {
        Repository repository = new Repository();

        repository.setId((String) repoData.get("id"));
        repository.setName((String) repoData.get("name"));
        repository.setDescription((String) repoData.get("description"));

        Object primaryLanguageObj = repoData.get("primaryLanguage");
        if (primaryLanguageObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> primaryLanguage = (Map<String, Object>) primaryLanguageObj;
            repository.setLanguage((String) primaryLanguage.get("name"));
        }

        Object starCount = repoData.get("stargazerCount");
        repository.setStargazerCount(starCount instanceof Integer ? (Integer) starCount : 0);

        Object forkCount = repoData.get("forkCount");
        repository.setForkCount(forkCount instanceof Integer ? (Integer) forkCount : 0);

        Object isPrivate = repoData.get("isPrivate");
        repository.setIsPrivate(isPrivate instanceof Boolean ? (Boolean) isPrivate : false);

        Object createdAt = repoData.get("createdAt");
        if (createdAt instanceof String) {
            try {
                repository.setCreatedAt(Instant.parse((String) createdAt));
            } catch (Exception e) {
                repository.setCreatedAt(Instant.now()); // fallback
            }
        }

        Object updatedAt = repoData.get("updatedAt");
        if (updatedAt instanceof String) {
            try {
                repository.setUpdatedAt(Instant.parse((String) updatedAt));
            } catch (Exception e) {
                repository.setUpdatedAt(Instant.now()); // fallback
            }
        }

        return repository;
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