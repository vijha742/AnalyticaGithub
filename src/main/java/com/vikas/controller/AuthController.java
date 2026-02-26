package com.vikas.controller;

import com.vikas.dto.AuthResponse;
import com.vikas.dto.SocialLoginRequest;
import com.vikas.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody SocialLoginRequest req) {
        System.out.println("Triggered Sign in");
        AuthResponse response = authService.authenticate(req);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-jwt")
    public ResponseEntity<?> refreshJwt(@CookieValue(name = "refreshToken") String req) {
        System.out.println("Triggered refresh jwt");
        AuthResponse response = authService.refreshAccessToken(req);
        return ResponseEntity.ok(response);
    }
}
