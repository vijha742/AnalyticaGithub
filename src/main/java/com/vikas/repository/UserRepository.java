package com.vikas.repository;

import com.vikas.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByGithubUsername(String githubUsername);

    List<User> findTop10ByOrderByTotalContributionsDesc();
}
