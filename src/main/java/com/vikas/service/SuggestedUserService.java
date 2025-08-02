 package com.vikas.service;

 import com.vikas.model.SuggestedUser;
 import com.vikas.model.User;

 import java.util.List;
 import java.util.Optional;
 import java.util.UUID;

 public interface SuggestedUserService {
 SuggestedUser suggestUser(String githubUsername, String team);
//
// SuggestedUser refreshUserData(String githubUsername);
 List<SuggestedUser> getAllActiveUsers(String team);
//
// Optional<SuggestedUser> getUserByUsername(String githubUsername);
//
// List<SuggestedUser> getActiveSuggestedUsersWithTimeoutForUser(String
// username);
//
// void deactivateUser(Long id);
//
// boolean isUserSuggested(String githubUsername);
 }
