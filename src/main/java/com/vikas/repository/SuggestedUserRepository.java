package com.vikas.repository;

import com.vikas.model.SuggestedUser;
import com.vikas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SuggestedUserRepository extends JpaRepository<SuggestedUser, UUID> {
    Optional<SuggestedUser> findByGithubUsername(String githubUsername);

    List<SuggestedUser> findByActiveTrueAndSuggestedByAndTeam(User suggestedBy, String team);

    SuggestedUser findByGithubUsernameAndTeam(String githubUsername, String team);

    List<SuggestedUser> findByActiveTrue();

    boolean existsByGithubUsernameAndSuggestedByAndTeam(String githubUsername);
}
