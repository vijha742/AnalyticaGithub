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

    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
    // suggestedUserService.deactivateUser(id);
    // return ResponseEntity.ok().build();
    // }

    // @GetMapping("/check/{githubUsername}")
    // public ResponseEntity<Boolean> isUserSuggested(@PathVariable String
    // githubUsername) {
    // return
    // ResponseEntity.ok(suggestedUserService.isUserSuggested(githubUsername));
    // }
}
