package com.vikas.repository;

import com.vikas.model.SuggestedGithubRepository;
import com.vikas.model.User;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SuggestedUserRepoDataRepository
        extends JpaRepository<SuggestedGithubRepository, UUID> {
    List<SuggestedGithubRepository> findByUserId(UUID userId);

    @Modifying
    @Transactional
    @Query(
            "DELETE FROM SuggestedGithubRepository sgr WHERE sgr.user.id IN (SELECT su.id FROM"
                    + " SuggestedUser su WHERE su.suggestedBy = :user AND su.team = :team)")
    void deleteAllByUserAndTeam(@Param("user") User user, @Param("team") String team);
}
