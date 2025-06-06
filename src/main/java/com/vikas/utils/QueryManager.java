package com.vikas.utils;

public class QueryManager {

    public QueryManager() {};

    public String fetchUserData() {
        return """
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
                                                viewerCanAdminister
                                                createdAt
                                                updatedAt
                                                repositoryTopics(first: 100) {
                                                    nodes {
                                                        topic {
                                                            name
                                                        }
                                                    }
                                                }
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
    }
}
