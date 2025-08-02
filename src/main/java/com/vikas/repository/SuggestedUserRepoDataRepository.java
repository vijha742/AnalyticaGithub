package com.vikas.repository;

import com.vikas.model.SuggestedGithubRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SuggestedUserRepoDataRepository extends JpaRepository<SuggestedGithubRepository, UUID> {
    List<SuggestedGithubRepository> findByUserId(UUID userId);
}
