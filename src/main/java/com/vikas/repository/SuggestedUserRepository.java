package com.vikas.repository;

import com.vikas.model.SuggestedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SuggestedUserRepository extends JpaRepository<SuggestedUser, Long> {
    Optional<SuggestedUser> findByGithubUsername(String githubUsername);
    List<SuggestedUser> findByActiveTrue(); // Changed from findByIsActiveTrue
    boolean existsByGithubUsername(String githubUsername);
}