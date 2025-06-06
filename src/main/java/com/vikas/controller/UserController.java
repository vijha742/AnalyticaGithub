package com.vikas.controller;

import com.vikas.model.timeseries.ContributionCalendar;
import com.vikas.service.GitHubService;
import com.vikas.service.impl.AnalyticsServiceImpl;
import com.vikas.utils.GithubGraphQLClient;
import com.vikas.utils.QueryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final GitHubService gitHubService;
    private final AnalyticsServiceImpl analyticsService;

    @Autowired
    public UserController(GitHubService gitHubService, AnalyticsServiceImpl analyticsService) {
        this.gitHubService = gitHubService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserData(@PathVariable String username) {
        return gitHubService.fetchUserData(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        ContributionCalendar data =  analyticsService.getContributionCalendar("vijha742");
        if(data != null) {
            return ResponseEntity.ok(data);
        } else return ResponseEntity.notFound().build();
    }

//    @GetMapping("/rate-limit")
//    public ResponseEntity<?> getRateLimit() {
//        return ResponseEntity.ok(gitHubService.getRemainingRateLimit());
//    }
} 