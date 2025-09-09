package com.vikas.service;

import com.vikas.dto.AuthDTO;
import com.vikas.dto.UserComparisonDTO;
import com.vikas.model.User;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface GitHubService {

    @Transactional
    User findOrCreateUser(AuthDTO githubUser);

    User findUser(String githubUsername);

    // Optional<User> addUserData(String githubUsername);
    // void updateUserData(User user);

    // boolean isRateLimitExceeded();

    List<User> searchUsers(String query, int limit);

    // User refreshUserData(String githubUsername);

    // Time series data for charts
    // ContributionCalendar getContributionTimeSeries(String username);

    Optional<User> findByUsername(String username);

    List<String> createTeam(String team);

    List<User> getLeaderboard();

    UserComparisonDTO compareTwoUsers(String githubUsername1, String githubUsername2);
}
