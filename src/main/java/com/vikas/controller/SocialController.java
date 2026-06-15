package com.vikas.controller;

import com.vikas.service.GitHubService;
import com.vikas.service.SuggestedUserService;

import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/social")
public class SocialController {

    private final GitHubService userService;
    private final SuggestedUserService suggestedUserService;

    /**
     * Global leaderboard — top contributors across all tracked users.
     * Publicly accessible: useful as a landing-page showcase without login.
     */
    @GetMapping("/leaderboard/global")
    public ResponseEntity<?> globalLeaderboard() {
        return ResponseEntity.ok(userService.getLeaderboard());
    }

    /**
     * Local leaderboard — top contributors from the authenticated user's
     * suggested-user pool. Requires authentication because the result is
     * scoped to the requesting user's data.
     */
    @GetMapping("/leaderboard/local")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> localLeaderboard() {
        return ResponseEntity.ok(suggestedUserService.getLeaderboard());
    }

    /**
     * Peer matching — complementary skill set.
     * Requires authentication: matching is done against the user's own profile.
     */
    @GetMapping("/p-match/c")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> matchPeersComplementary() {
        return ResponseEntity.ok(suggestedUserService.getCompMatch());
    }

    /**
     * Peer matching — supplementary skill set.
     * Requires authentication: matching is done against the user's own profile.
     */
    @GetMapping("/p-match/s")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> matchPeersSupplementary() {
        return ResponseEntity.ok(suggestedUserService.getSuppMatch());
    }
}
