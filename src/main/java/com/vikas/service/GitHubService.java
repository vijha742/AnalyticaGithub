package com.vikas.service;

import com.vikas.model.GithubUser;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GitHubService {
    // void updateRateLimit(GitHubUserResponse.RateLimit limit);

    Optional<GithubUser> fetchUserData(String githubUsername);

    void updateUserData(GithubUser user);

//    boolean isRateLimitExceeded();

    List<GithubUser> searchUsers(String query, int limit, int offset);

    boolean verifyUserExists(String githubUsername);

    GithubUser refreshUserData(String githubUsername);

    // Time series data for charts
    Map<LocalDate, Integer> getContributionTimeSeries(
            String username,
            String timeFrame,
            LocalDate startDate,
            LocalDate endDate,
            List<String> contributionTypes);
}

