package com.vikas.repository;

import com.vikas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByGithubUsername(String githubUsername);

    Optional<User> findById(Long id);

    boolean existsByGithubUsername(String githubUsername);
}
