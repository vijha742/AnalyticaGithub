package com.vikas.controller;

import com.vikas.model.Contribution;
import com.vikas.model.User;
import com.vikas.service.AnalyticsService;
import com.vikas.service.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<?> contributionsTimeSeries(@PathVariable String username, @RequestParam String mode) {
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
        return gitHubService.searchUsers(keyword, limit != null ? limit : 10);
    }
    @PostMapping("/team")
    public ResponseEntity<List<String>> createTeam(@RequestParam String teamName) {
        List<String> teams = gitHubService.createTeam(teamName);
        if (teams != null) {
            return ResponseEntity.ok(teams);
        } else return ResponseEntity.notFound().build();

    }
}
