package com.vikas.service;

import com.vikas.model.SuggestedUser;
import com.vikas.model.GithubUser;
import java.util.List;
import java.util.Optional;

public interface SuggestedUserService {
    SuggestedUser suggestUser(String githubUsername, String suggestedBy);
    SuggestedUser refreshUserData(String githubUsername);
    List<SuggestedUser> getAllActiveUsers();
    Optional<SuggestedUser> getUserByUsername(String githubUsername);
    List<GithubUser> getActiveSuggestedUsers();
    void deactivateUser(Long id);
    boolean isUserSuggested(String githubUsername);
} 