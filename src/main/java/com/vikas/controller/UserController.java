package com.vikas.controller;

import com.vikas.model.User;
import com.vikas.service.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/users")
@RequiredArgsConstructor
public class UserController {

    private final GitHubService gitHubService;

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
//    @GetMapping("/test")
//    public ResponseEntity<String> test() {
//        return ResponseEntity.ok("ping...");
//    }
//
//    @GetMapping("/{username}/contrib-cal")
//    public ResponseEntity<?> contributionsTimeSeries(@PathVariable String
//                                                             username) {
//        ContributionCalendar data =
//                gitHubService.getContributionTimeSeries(username);
//        if (data != null) {
//            return ResponseEntity.ok(data);
//        } else return ResponseEntity.notFound().build();
//    }
//
//    // @GetMapping("/rate-limit")
//    // public ResponseEntity<?> getRateLimit() {
//    // return ResponseEntity.ok(gitHubService.getRemainingRateLimit());
//    // }
}
