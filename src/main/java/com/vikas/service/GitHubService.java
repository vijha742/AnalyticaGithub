 package com.vikas.service;

 import com.vikas.dto.AuthDTO;
 import com.vikas.model.User;
 import org.springframework.security.core.userdetails.UserDetails;
 import
 org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

 public interface GitHubService {
// void updateRateLimit(GitHubUserResponse.RateLimit limit);

 User findOrCreateUser(AuthDTO githubUser);

 User findUser(String githubUsername);

// Optional<User> addUserData(String githubUsername);
// void updateUserData(User user);

//  boolean isRateLimitExceeded();

// List<User> searchUsers(String query, int limit, int offset);

// User refreshUserData(String githubUsername);

 // Time series data for charts
// ContributionCalendar getContributionTimeSeries(String username);

 Optional<User> findByUsername(String username);

  List<String> createTeam(String team);

}
