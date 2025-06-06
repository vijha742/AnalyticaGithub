package com.vikas.utils;

public class QueryManager {

    public QueryManager() {}

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

    public String searchUsers() {
        return """
                query($query: String!, $first: Int!, $after: String) {
                    search(query: $query, type: USER, first: $first, after: $after) {
                        nodes {
                            ... on User {
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
                            }
                        }
                    }
                }
                """;
    }

    public String getContributionCalendar() {
        return """
               query($username: String!) {
                 user(login: $username) {
                   contributionsCollection {
                     contributionCalendar {
                       totalContributions
                       weeks {
                         firstDay
                         contributionDays {
                           date
                           contributionCount
                           contributionLevel
                         }
                       }
                     }
                   }
                 }
               }
               """;
    }
}
