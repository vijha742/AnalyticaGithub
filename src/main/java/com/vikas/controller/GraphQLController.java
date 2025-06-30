package com.vikas.controller;

import com.vikas.model.GithubUser;
import com.vikas.model.SuggestedUser;
import com.vikas.model.timeseries.ContributionCalendar;
import com.vikas.model.timeseries.ContributionTimeSeries;
import com.vikas.model.timeseries.TimeSeriesDataPoint;
import com.vikas.service.GitHubService;
import com.vikas.service.SuggestedUserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class GraphQLController {

    private final GitHubService gitHubService;
    private final SuggestedUserService suggestedUserService;

    public GraphQLController(GitHubService gitHubService, SuggestedUserService suggestedUserService) {
        this.gitHubService = gitHubService;
        this.suggestedUserService = suggestedUserService;
    }

    @QueryMapping
    public Optional<GithubUser> user(@Argument String username) {
        return gitHubService.fetchUserData(username);
    }

    @QueryMapping
    public List<GithubUser> users(@Argument Integer limit, @Argument Integer offset) {
        // TODO: Implement pagination
        return List.of();
    }

    @QueryMapping
    public List<GithubUser> searchUsers(@Argument String query, @Argument Integer limit, @Argument Integer offset) {
        return gitHubService.searchUsers(query, limit != null ? limit : 10, offset != null ? offset : 0);
    }

    // TODO: Implement Ratelimit query..
//    @QueryMapping
//    public RateLimit rateLimit() {
//        return new RateLimit(
//            gitHubService.getRemainingRateLimit(),
//            5000, // TODO: Get from configuration
//            "2024-03-20T00:00:00Z" // TODO: Calculate reset time
//        );
//    }

    @MutationMapping
    public SuggestedUser suggestUser(@Argument String githubUsername, @Argument String suggestedBy) {
        return suggestedUserService.suggestUser(githubUsername, suggestedBy);
    }

    @MutationMapping
    public SuggestedUser refreshUserData(@Argument String githubUsername) {
        return suggestedUserService.refreshUserData(githubUsername);
    }

    @QueryMapping
    public List<SuggestedUser> suggestedUsers() {
        return suggestedUserService.getAllActiveUsers();
    }

    record RateLimit(int remaining, int limit, String resetAt) {}
}