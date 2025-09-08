package com.vikas.service;

import com.vikas.dto.MatchedPeerDTO;
import com.vikas.dto.UserComparisonDTO;
import com.vikas.model.SuggestedUser;

import java.util.List;
import java.util.UUID;

public interface SuggestedUserService {
    SuggestedUser suggestUser(String githubUsername, String team);

    //
    SuggestedUser refreshUserData(String githubUsername, String team);

    List<SuggestedUser> getAllActiveUsers(String team);

    //
    // Optional<SuggestedUser> getUserByUsername(String githubUsername);
    //
    // List<SuggestedUser> getActiveSuggestedUsersWithTimeoutForUser(String
    // username);
    //
    boolean deactivateUser(UUID id);

    UserComparisonDTO compareTwoUsers(String githubUsername1, String githubUsername2);

    // boolean isUserSuggested(String githubUsername);
    List<SuggestedUser> getLeaderboard();

    List<MatchedPeerDTO> getCompMatch();

    List<MatchedPeerDTO> getSuppMatch();
}
