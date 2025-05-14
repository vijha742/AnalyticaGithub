package com.vikas.service;

import com.vikas.model.GithubUser;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.time.LocalDate;

public interface GitHubService {
    Optional<GithubUser> fetchUserData(String githubUsername);
    void updateUserData(GithubUser user);
    boolean isRateLimitExceeded();
    int getRemainingRateLimit();
    List<GithubUser> searchUsers(String query, int limit, int offset);
    
    // New methods
    boolean verifyUserExists(String githubUsername);
    GithubUser refreshUserData(String githubUsername);
    
    // Time series data for charts
    Map<LocalDate, Integer> getContributionTimeSeries(String username, String timeFrame, LocalDate startDate, LocalDate endDate, List<String> contributionTypes);
}