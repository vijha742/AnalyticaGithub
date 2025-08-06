package com.vikas.security;

import com.vikas.model.User;
import com.vikas.service.GitHubService;
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
import java.util.Arrays;
import java.util.Collection;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTAuthenticationFilter extends OncePerRequestFilter {

        private final JWTService jwtService;
        private final GitHubService gitHubService;

        @Override
        protected void doFilterInternal(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain filterChain) throws ServletException, IOException {
                System.out.println("Hey.... JWTAuthenticationFilter is invoked");
                final String authHeader = request.getHeader("Authorization");
                final String jwt;
                final String username;

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        filterChain.doFilter(request, response);
                        System.out.println("Authorization header is missing or does not start with 'Bearer '");
                        return;
                }

                jwt = authHeader.substring(7);
                 log.debug("Extracted JWT: {}", jwt);

                try {
                        log.info("going to extract Username now....");
                        username = jwtService.extractUsername(jwt);
                } catch (Exception e) {
                         log.warn("JWT extraction failed: {}", e.getMessage());
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                        return;
                }

                if (username != null &&
                                SecurityContextHolder.getContext().getAuthentication() == null) {
                                User userDetails = gitHubService.findUser(username);
                        Collection<GrantedAuthority> authorities = Arrays.asList(
                                new SimpleGrantedAuthority("ROLE_USER")
                        );
                        if (jwtService.isTokenValid(jwt, userDetails)) {
                                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                                userDetails, null, authorities);
                                authToken.setDetails(
                                                new WebAuthenticationDetailsSource().buildDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(authToken);
                                 log.info("User '{}' authenticated successfully via JWT.", username);
                        } else {
                                 log.warn("JWT token is invalid for user: {}", username);
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                                return;
                        }
                }
                filterChain.doFilter(request, response);
        }
}
