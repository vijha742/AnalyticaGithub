package com.vikas.service;

import com.vikas.model.GithubUser;
import com.vikas.model.User;
import com.vikas.model.timeseries.ContributionCalendar;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

public interface GitHubService {
    // void updateRateLimit(GitHubUserResponse.RateLimit limit);

 User findOrCreateUser(AuthDTO githubUser);

 User findOrCreateUser(String githubUsername);

    void updateUserData(GithubUser user);

    // boolean isRateLimitExceeded();

    List<GithubUser> searchUsers(String query, int limit, int offset);

    GithubUser refreshUserData(String githubUsername);

    // Time series data for charts
    ContributionCalendar getContributionTimeSeries(String username);

 Optional<User> findByUsername(String username);

}
