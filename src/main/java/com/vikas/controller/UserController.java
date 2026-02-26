package com.vikas.controller;

import com.vikas.model.Contribution;
import com.vikas.model.User;
import com.vikas.service.AnalyticsService;
import com.vikas.service.GitHubService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/users")
@RequiredArgsConstructor
public class UserController {

    private final GitHubService gitHubService;
    private final AnalyticsService analyticsService;

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserData(@PathVariable String username) {
        System.out.println("Triggered /username");
        User user = gitHubService.findUser(username);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    //
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("ping...");
    }

    //
    @GetMapping("/{username}/contrib-cal")
    public ResponseEntity<?> contributionsTimeSeries(
            @PathVariable String username, @RequestParam String mode) {
        System.out.println("Triggered /contrib-cal");
        Contribution data = analyticsService.getContributions(username, mode);
        if (data != null) {
            return ResponseEntity.ok(data);
        } else
            return ResponseEntity.notFound().build();
    }

    //
    // // @GetMapping("/rate-limit")
    // // public ResponseEntity<?> getRateLimit() {
    // // return ResponseEntity.ok(gitHubService.getRemainingRateLimit());
    // // }

    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam String keyword, @RequestParam Integer limit) {
        System.out.println("Triggered /search");
        return gitHubService.searchUsers(keyword, limit != null ? limit : 10);
    }

    @GetMapping("/team")
    public ResponseEntity<List<String>> getTeam() {
        System.out.println("Triggered /team get");
        List<String> teams = gitHubService.getTeams();
        if (teams != null) {
            return ResponseEntity.ok(teams);
        } else
            return ResponseEntity.notFound().build();
    }

    @PostMapping("/team")
    public ResponseEntity<List<String>> createTeam(@RequestParam String teamName) {
        System.out.println("Triggered /team post");
        List<String> teams = gitHubService.createTeam(teamName);
        if (teams != null) {
            return ResponseEntity.ok(teams);
        } else
            return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/team")
    public ResponseEntity<List<String>> deleteTeam(@RequestParam String teamName) {
        List<String> teams = gitHubService.deleteTeam(teamName);
        if (teams != null) {
            return ResponseEntity.ok(teams);
        } else
            return ResponseEntity.notFound().build();
    }

    // HACK: In the current implementation I am using that every user that user is
    // gonna compare
    // would exist in SuggestedUserRepository...which can result in Mishap soon.
    @GetMapping("/compare")
    public ResponseEntity<?> compareTwoUsers(
            @RequestParam String User1, @RequestParam String User2) {
        try {
            return ResponseEntity.ok(gitHubService.compareTwoUsers(User1, User2));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Comparison failed: " + e.getMessage());
        }
    }
}
