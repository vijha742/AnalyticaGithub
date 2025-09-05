package com.vikas.controller;

import com.vikas.model.SuggestedUser;
import com.vikas.service.SuggestedUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/suggested-users")
public class SuggestedUserController {

    private final SuggestedUserService suggestedUserService;

    public SuggestedUserController(SuggestedUserService suggestedUserService) {
        this.suggestedUserService = suggestedUserService;
    }

    @PostMapping
    public ResponseEntity<SuggestedUser> suggestUser(
            @RequestParam String githubUsername,
            @RequestParam String team) {
        return ResponseEntity.ok(suggestedUserService.suggestUser(githubUsername, team));
    }

    @GetMapping
    public ResponseEntity<List<SuggestedUser>> getActiveSuggestedUsers(@RequestParam String team) {
        try {
            List<SuggestedUser> users = suggestedUserService.getAllActiveUsers(team);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Failed to fetch suggested users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ArrayList<>());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<SuggestedUser> refreshUser(
            @RequestParam String githubUsername,
            @RequestParam String team) {
        SuggestedUser user = suggestedUserService.refreshUserData(githubUsername, team);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

     @DeleteMapping("/{id}")
     public ResponseEntity<Void> deactivateUser(@PathVariable UUID id) {
        boolean response = suggestedUserService.deactivateUser(id);
        if (response) return ResponseEntity.ok().build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
     }
    // HACK: In the current implementation I am using that every user that user is gonna compare
    // would exist in SuggestedUserRepository...which can result in Mishap soon.
    @GetMapping("/compare")
    public ResponseEntity<?> compareTwoUsers(
            @RequestParam String User1, @RequestParam String User2) {
        try {
            return ResponseEntity.ok(suggestedUserService.compareTwoUsers(User1, User2));
        } catch (Exception e) {
            log.error("Comparison failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Comparison failed: " + e.getMessage());
        }
    }

    // @GetMapping("/check/{githubUsername}")
    // public ResponseEntity<Boolean> isUserSuggested(@PathVariable String
    // githubUsername) {
    // return
    // ResponseEntity.ok(suggestedUserService.isUserSuggested(githubUsername));
    // }
}
