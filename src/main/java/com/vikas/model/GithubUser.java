package com.vikas.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import lombok.Data;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class GithubUser {
    @Id
    private String id;

    @Column(unique = true, nullable = false)
    private String githubUsername;

    @Column(nullable = false)
    private String name;

    private String email;
    private String avatarUrl;
    private String bio;
    private Integer followersCount;
    private Integer followingCount;
    private Integer publicReposCount;
    private Integer totalContributions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Repository> repositories;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Contribution> contributions;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = Instant.now();
    }

    public static GithubUser mapToUser(Map<String, Object> userData) {
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
        return user;
    }

    private static Repository mapRepository(Map<String, Object> repoData) {
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
}
