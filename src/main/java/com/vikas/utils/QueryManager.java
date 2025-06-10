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

    public String analyzeReadmeQuality() {
        return """            
             query($owner: String!, $name: String!) {
                        repository(owner: $owner, name: $name) {
                          object(expression: "HEAD:README.md") {
                            ... on Blob {
                              text
                              commitUrl
                            }
                          }
                          defaultBranchRef {
                            target {
                              ... on Commit {
                                history(path: "README.md") {
                                  nodes {
                                    committedDate
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
            """;
    }

    public String getCodeMetrics() {
        return """
              query($owner: String!, $name: String!) {
                repository(owner: $owner, name: $name) {
                languages(first: 100, orderBy: {field: SIZE, direction: DESC}) {
                  edges {
                  size
                  node {
                    name
                  }
                  }
                  totalSize
                }
                }
              }
              """;
    }

    public String getTechnicalProfile() {
        return """
                query($owner: String!) {
                    user(login: $owner) {
                      repositories(first: 100) {
                        nodes {
                          name
                          createdAt
                          updatedAt
                          languages(first: 100, orderBy: {field: SIZE, direction: DESC}) {
                            edges {
                              size
                              node {
                                name
                              }
                            }
                          }
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
              """;
    }
}
