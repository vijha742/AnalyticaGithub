package com.vikas.service.impl;

import com.vikas.model.User;
import com.vikas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByGithubUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Build Spring Security's UserDetails object from our User entity
        return new org.springframework.security.core.userdetails.User(
                user.getGithubUsername(),
                "",
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().toString()))
        );
    }
}
