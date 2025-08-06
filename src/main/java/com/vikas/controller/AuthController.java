 package com.vikas.controller;

 import java.security.DrbgParameters.Reseed;
 import java.util.Map;

 import com.vikas.dto.AuthResponse;
 import com.vikas.service.AuthService;
 import lombok.RequiredArgsConstructor;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.annotation.PostMapping;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RestController;

 import com.vikas.dto.SocialLoginRequest;

 @RequestMapping("/auth")
 @RestController
 @RequiredArgsConstructor
 public class AuthController {

    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody SocialLoginRequest req) {
        System.out.println("SignInRequest: " + req);
        AuthResponse response = authService.authenticate(req);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-jwt")
     public  ResponseEntity<?> refreshJwt(@RequestBody String req) {
            System.out.println("RefreshJwtRequest: " + req);
            AuthResponse response = authService.refreshAccessToken(req);
            return ResponseEntity.ok(response);
    }

 }
