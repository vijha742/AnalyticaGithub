package com.vikas.controller;

import com.vikas.model.User;
import com.vikas.service.GitHubService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final GitHubService gitHubService;

    public UserController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserData(@PathVariable String username) {
        return gitHubService.fetchUserData(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rate-limit")
    public ResponseEntity<?> getRateLimit() {
        return ResponseEntity.ok(gitHubService.getRemainingRateLimit());
    }
} 