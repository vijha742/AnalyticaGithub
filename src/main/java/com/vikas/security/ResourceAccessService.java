package com.vikas.security;

import com.vikas.model.SuggestedUser;
import com.vikas.model.User;
import com.vikas.repository.SuggestedUserRepository;
import com.vikas.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service("resourceAccessService")
@RequiredArgsConstructor
public class ResourceAccessService {

    private final SuggestedUserRepository suggestedUserRepository;
    private final UserRepository userRepository;

    public boolean canDeactivateSuggestedUser(Authentication authentication, UUID suggestedUserId) {
        Optional<User> authenticatedUser = resolveCurrentUser(authentication);
        if (authenticatedUser.isEmpty()) {
            return false;
        }

        return suggestedUserRepository
                .findById(suggestedUserId)
                .map(SuggestedUser::getSuggestedBy)
                .map(User::getId)
                .filter(ownerId -> ownerId.equals(authenticatedUser.get().getId()))
                .isPresent();
    }

    public boolean canCreateTeam(Authentication authentication) {
        return resolveCurrentUser(authentication).isPresent();
    }

    public boolean canDeleteTeam(Authentication authentication, String teamName) {
        if (teamName == null || teamName.isBlank()) {
            return false;
        }

        return resolveCurrentUser(authentication)
                .map(User::getTeams)
                .filter(teams -> teams != null && teams.contains(teamName))
                .isPresent();
    }

    private Optional<User> resolveCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User userPrincipal)) {
            return Optional.empty();
        }

        if (userPrincipal.getGithubUsername() != null) {
            return userRepository.findByGithubUsername(userPrincipal.getGithubUsername());
        }

        UUID userId = userPrincipal.getId();
        if (userId == null) {
            return Optional.empty();
        }

        return userRepository.findById(userId);
    }
}
