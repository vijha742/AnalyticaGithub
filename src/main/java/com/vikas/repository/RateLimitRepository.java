package com.vikas.repository;

import com.vikas.model.RateLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RateLimitRepository extends JpaRepository<RateLimit, Long> {
    Optional<RateLimit> findTopByOrderByCreatedAtDesc();
}