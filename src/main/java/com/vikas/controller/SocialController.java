package com.vikas.controller;

import com.vikas.service.GitHubService;
import com.vikas.service.SuggestedUserService;

import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/social")
public class SocialController {
    private final GitHubService userService;
    private final SuggestedUserService suggestedUserService;

    // scope : [local(suggested-users), global(users)]
    @GetMapping("/leaderboard")
    public ResponseEntity<?> leaderboard(@RequestParam String scope) {
        if (scope.equals("local")) {
            return ResponseEntity.ok(suggestedUserService.getLeaderboard());
        } else if (scope.equals("global")) {
            return ResponseEntity.ok(userService.getLeaderboard());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/p-match/c")
    public ResponseEntity<?> matchPeersComplementary() {
        return ResponseEntity.ok(suggestedUserService.getCompMatch());
    }

    @GetMapping("/p-match/s")
    public ResponseEntity<?> matchPeersSupplementary() {
        return ResponseEntity.ok(suggestedUserService.getSuppMatch());
    }
}
