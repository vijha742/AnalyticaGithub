package com.vikas.dto;

import lombok.Data;
import java.util.List;

@Data
public class GitHubSearchResponse {
    private SearchData data;

    @Data
    public static class SearchData {
        private Search search;
    }

    @Data
    public static class Search {
        private List<User> nodes;
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
} 