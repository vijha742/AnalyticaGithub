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
                                  createdAt
                                  updatedAt
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
             query($owner: String!) {
                      user(login: $owner) {
                        repositories(first: 100) {
                            nodes {
                                name
                                readme1: object(expression: "HEAD:README.md") {
                                    ... on Blob {
                                      text
                                      commitUrl
                                    }
                                }
                                readme2: object(expression: "HEAD:Readme.md") {
                                    ... on Blob {
                                      text
                                      commitUrl
                                    }
                                }
                                readme3: object(expression: "HEAD:readme.md") {
                                    ... on Blob {
                                      text
                                      commitUrl
                                    }
                                }
                                readme4: object(expression: "HEAD:ReadMe.md") {
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
                      }
             }
            """;
    }

    public String getCodeMetrics() {
        return """
              query($login: String!) {
              user(login: $login) {
                repositories(first: 100, privacy: PUBLIC, orderBy: {field: UPDATED_AT, direction: DESC}) {
                 nodes {
                         name
                         owner {
                           login
                         }
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

    public String getImpactfulRepository() {
        return """
                query($username: String!) {
                  user(login: $username) {
                    repositories(first: 100, orderBy: {field: STARGAZERS, direction: DESC}) {
                      totalCount
                      nodes {
                        name
                        stargazerCount
                        forkCount
                        watchers {
                          totalCount
                        }
                        primaryLanguage {
                          name
                        }
                        languages(first: 10) {
                          edges {
                            node {
                              name
                            }
                          }
                        }
                        createdAt
                        dependencyGraphManifests(first: 1) {
                          nodes {
                            dependents(first: 1) {
                              totalCount
                            }
                          }
                        }
                        stargazers(first: 100, orderBy: {field: STARRED_AT, direction: ASC}) {
                          edges {
                            starredAt
                          }
                        }
                      }
                    }
                    followers {
                      totalCount
                    }
                  }
                }
                
                """;
    }
}
