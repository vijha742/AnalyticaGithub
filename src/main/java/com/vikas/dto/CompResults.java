package com.vikas.dto;

import com.vikas.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompResults {
    private String followersResult;
    private String publicReposResult;
    private String totalContributionsResult;
    private String pullRequestsResult;
    private String issuesResult;
    private String commitsResult;

    // Add more fields as necessary

    public CompResults(User user1, User user2) {
        this.followersResult = compareValues(user1.getFollowersCount(), user2.getFollowersCount());
        this.publicReposResult =
                compareValues(user1.getPublicReposCount(), user2.getPublicReposCount());
        this.totalContributionsResult =
                compareValues(user1.getTotalContributions(), user2.getTotalContributions());
    }

    // Can be replaced with Enum for better type safety
    private String compareValues(int val1, int val2) {
        if (val1 > val2) return "User 1";
        else if (val1 < val2) return "User 2";
        else return "Both are equal";
    }
}
