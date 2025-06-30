package com.vikas.controller;

import com.vikas.model.SuggestedUser;
import com.vikas.model.GithubUser;
import com.vikas.service.SuggestedUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
            @RequestParam String suggestedBy) {
        return ResponseEntity.ok(suggestedUserService.suggestUser(githubUsername, suggestedBy));
    }

    @GetMapping
    public ResponseEntity<List<GithubUser>> getActiveSuggestedUsers() {
        return ResponseEntity.ok(suggestedUserService.getActiveSuggestedUsers());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        suggestedUserService.deactivateUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check/{githubUsername}")
    public ResponseEntity<Boolean> isUserSuggested(@PathVariable String githubUsername) {
        return ResponseEntity.ok(suggestedUserService.isUserSuggested(githubUsername));
    }
} 