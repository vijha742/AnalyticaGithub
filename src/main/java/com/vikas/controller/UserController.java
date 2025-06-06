package com.vikas.controller;

import com.vikas.model.CodeMetrics;
import com.vikas.model.ReadmeQuality;
import com.vikas.service.GitHubService;
import com.vikas.service.impl.RepositoryAnalyticsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final GitHubService gitHubService;
    private final RepositoryAnalyticsServiceImpl analyticsService;

    @Autowired
    public UserController(GitHubService gitHubService, RepositoryAnalyticsServiceImpl analyticsService) {
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
        CodeMetrics data =  analyticsService.getCodeMetrics("vijha742", "Analytica_frontend");
        if(data != null) {
            return ResponseEntity.ok(data);
        } else return ResponseEntity.notFound().build();
    }

//    @GetMapping("/rate-limit")
//    public ResponseEntity<?> getRateLimit() {
//        return ResponseEntity.ok(gitHubService.getRemainingRateLimit());
//    }
} 