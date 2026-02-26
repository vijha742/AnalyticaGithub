package com.vikas.repository;

import com.vikas.model.SuggestedUser;
import com.vikas.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SuggestedUserRepository extends JpaRepository<SuggestedUser, UUID> {
    Optional<SuggestedUser> findByGithubUsername(String githubUsername);

    SuggestedUser findFirstByGithubUsername(String githubUsername);

    List<SuggestedUser> findByActiveTrueAndSuggestedByAndTeam(User suggestedBy, String team);

    SuggestedUser findByGithubUsernameAndTeam(String githubUsername, String team);

    SuggestedUser findByGithubUsernameAndSuggestedByAndTeam(
            String githubUsername, User suugestedBy, String team);

    List<SuggestedUser> findByActiveTrue();

    boolean existsByGithubUsernameAndSuggestedByAndTeam(
            String githubUsername, User suggestedBy, String team);

    List<SuggestedUser> findTop10BySuggestedByAndActiveTrueOrderByTotalContributionsDesc(
            User suggestedBy);

    @Modifying
    @Transactional
    @Query("DELETE FROM SuggestedUser su WHERE su.suggestedBy = :user AND su.team = :team")
    void deleteAllByUserAndTeam(@Param("user") User user, @Param("team") String team);
}
