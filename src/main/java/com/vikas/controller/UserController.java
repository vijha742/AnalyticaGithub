package com.vikas.controller;

import com.vikas.model.Contribution;
import com.vikas.model.User;
import com.vikas.service.AnalyticsService;
import com.vikas.service.GitHubService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/public/users")
@RequiredArgsConstructor
public class UserController {

    private final GitHubService gitHubService;
    private final AnalyticsService analyticsService;

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserData(
            @PathVariable 
            @NotBlank(message = "Username cannot be blank")
            @Pattern(regexp = "^[a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38}$", 
                    message = "Invalid GitHub username format") 
            String username) {
        log.debug("Fetching user data for: {}", username);
        User user = gitHubService.findUser(username);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("ping...");
    }

    @GetMapping("/{username}/contrib-cal")
    public ResponseEntity<?> contributionsTimeSeries(
            @PathVariable 
            @NotBlank(message = "Username cannot be blank")
            @Pattern(regexp = "^[a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38}$", 
                    message = "Invalid GitHub username format")
            String username, 
            @RequestParam 
            @Pattern(regexp = "^(daily|weekly|monthly)$", message = "Mode must be daily, weekly, or monthly")
            String mode) {
        log.debug("Fetching contribution calendar for: {} with mode: {}", username, mode);
        Contribution data = analyticsService.getContributions(username, mode);
        if (data != null) {
            return ResponseEntity.ok(data);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public List<User> searchUsers(
            @RequestParam 
            @NotBlank(message = "Keyword cannot be blank") 
            String keyword, 
            @RequestParam(required = false) 
            @Min(value = 1, message = "Limit must be at least 1")
            @Max(value = 100, message = "Limit cannot exceed 100")
            Integer limit) {
        log.debug("Searching users with keyword: {} and limit: {}", keyword, limit);
        return gitHubService.searchUsers(keyword, limit != null ? limit : 10);
    }

    @GetMapping("/team")
    public ResponseEntity<List<String>> getTeam() {
        log.debug("Fetching teams");
        List<String> teams = gitHubService.getTeams();
        if (teams != null) {
            return ResponseEntity.ok(teams);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/team")
    public ResponseEntity<List<String>> createTeam(
            @RequestParam 
            @NotBlank(message = "Team name cannot be blank") 
            String teamName) {
        log.debug("Creating team: {}", teamName);
        List<String> teams = gitHubService.createTeam(teamName);
        if (teams != null) {
            return ResponseEntity.ok(teams);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/team")
    public ResponseEntity<List<String>> deleteTeam(
            @RequestParam 
            @NotBlank(message = "Team name cannot be blank") 
            String teamName) {
        log.debug("Deleting team: {}", teamName);
        List<String> teams = gitHubService.deleteTeam(teamName);
        if (teams != null) {
            return ResponseEntity.ok(teams);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/compare")
    public ResponseEntity<?> compareTwoUsers(
            @RequestParam 
            @NotBlank(message = "User1 cannot be blank")
            @Pattern(regexp = "^[a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38}$", 
                    message = "Invalid GitHub username format for User1")
            String User1, 
            @RequestParam 
            @NotBlank(message = "User2 cannot be blank")
            @Pattern(regexp = "^[a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38}$", 
                    message = "Invalid GitHub username format for User2")
            String User2) {
        try {
            log.debug("Comparing users: {} and {}", User1, User2);
            return ResponseEntity.ok(gitHubService.compareTwoUsers(User1, User2));
        } catch (Exception e) {
            log.error("User comparison failed for {} and {}", User1, User2, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Comparison failed. Please try again later.");
        }
    }
}
