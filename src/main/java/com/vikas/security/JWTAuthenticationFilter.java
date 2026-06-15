package com.vikas.security;

import com.vikas.model.User;
import com.vikas.service.JWTService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Stateless JWT filter — no DB lookup on every request.
 *
 * The JJWT parser in JWTService.extractUsername() already verifies the HMAC
 * signature and the expiration claim. If either check fails the parser throws,
 * we catch it here and return 401.
 *
 * We embed the user's UUID in the "userId" JWT claim so we can reconstruct a
 * lightweight User principal (id + githubUsername) without touching the DB.
 * Services that need the full User entity (e.g. ResourceAccessService) do
 * their own targeted lookup only when the endpoint actually requires it.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String username;
        final String userId;

        // extractUsername() runs the full JJWT verification pipeline (signature +
        // expiry). Any tampered or expired token throws here — no extra isTokenValid
        // call is needed.
        try {
            username = jwtService.extractUsername(jwt);
            userId   = jwtService.extractUserId(jwt);
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
            return;
        }

        if (username != null && userId != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Build a lightweight principal from JWT claims only.
            // The principal carries enough identity for resource-ownership checks
            // (id + githubUsername); full User data is loaded on demand by services.
            User principal = new User();
            principal.setId(UUID.fromString(userId));
            principal.setGithubUsername(username);

            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
