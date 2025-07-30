package com.vikas.service;

import io.jsonwebtoken.Claims;
import com.vikas.model.User;
import java.util.Map;
import java.util.function.Function;

public interface JWTService {

    String extractUsername(String token);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    String generateToken(User userDetails);

    String generateToken(Map<String, Object> extraClaims, User userDetails);

    String generateRefreshToken(User userDetails);

    boolean isTokenValid(String token, User userDetails);

}
