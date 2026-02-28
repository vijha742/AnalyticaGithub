package com.vikas.controller;

import com.vikas.model.SuggestedUser;
import com.vikas.service.SuggestedUserService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/suggested-users")
@RequiredArgsConstructor
public class SuggestedUserController {

    private final SuggestedUserService suggestedUserService;

    @PostMapping
    public ResponseEntity<SuggestedUser> suggestUser(
            @RequestParam 
            @NotBlank(message = "GitHub username cannot be blank")
            @Pattern(regexp = "^[a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38}$", 
                    message = "Invalid GitHub username format")
            String githubUsername, 
            @RequestParam 
            @NotBlank(message = "Team name cannot be blank")
            String team) {
        log.debug("Suggesting user: {} for team: {}", githubUsername, team);
        return ResponseEntity.ok(suggestedUserService.suggestUser(githubUsername, team));
    }

    @GetMapping
    public ResponseEntity<List<SuggestedUser>> getActiveSuggestedUsers(
            @RequestParam 
            @NotBlank(message = "Team name cannot be blank")
            String team) {
        try {
            log.debug("Fetching suggested users for team: {}", team);
            List<SuggestedUser> users = suggestedUserService.getAllActiveUsers(team);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Failed to fetch suggested users for team: {}", team, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<SuggestedUser> refreshUser(
            @RequestParam 
            @NotBlank(message = "GitHub username cannot be blank")
            @Pattern(regexp = "^[a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38}$", 
                    message = "Invalid GitHub username format")
            String githubUsername, 
            @RequestParam 
            @NotBlank(message = "Team name cannot be blank")
            String team) {
        log.debug("Refreshing user data for: {} in team: {}", githubUsername, team);
        SuggestedUser user = suggestedUserService.refreshUserData(githubUsername, team);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID id) {
        log.debug("Deactivating suggested user with ID: {}", id);
        boolean response = suggestedUserService.deactivateUser(id);
        if (response) return ResponseEntity.ok().build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
