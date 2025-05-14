package com.vikas.dto;

import lombok.Data;
import java.util.List;

@Data
public class GitHubUserResponse {
    private ResponseData data;

    @Data
    public static class ResponseData {
        private User user;
        private RateLimit rateLimit;
    }

    @Data
    public static class User {
        private String id;
        private String login;
        private String name;
        private String email;
        private String avatarUrl;
        private String bio;
        private Followers followers;
        private Following following;
        private Repositories repositories;
        private ContributionsCollection contributionsCollection;
    }

    @Data
    public static class Followers {
        private int totalCount;
    }

    @Data
    public static class Following {
        private int totalCount;
    }

    @Data
    public static class Repositories {
        private int totalCount;
        private List<Repository> nodes;
    }

    @Data
    public static class Repository {
        private String id;
        private String name;
        private String description;
        private PrimaryLanguage primaryLanguage;
        private int stargazerCount;
        private int forkCount;
        private boolean isPrivate;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    public static class PrimaryLanguage {
        private String name;
    }

    @Data
    public static class ContributionsCollection {
        private int totalCommitContributions;
        private int totalPullRequestContributions;
        private int totalIssueContributions;
        private int totalRepositoryContributions;
    }

    @Data
    public static class RateLimit {
        private int limit;
        private int remaining;
        private String resetAt;
    }
} 