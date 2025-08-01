package com.vikas.repository;

import com.vikas.model.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContributionsRepository extends JpaRepository<Contribution, UUID> {
    Optional<Contribution> findByUserId(UUID userId);
    Boolean existsByUserId(UUID userId);
}
