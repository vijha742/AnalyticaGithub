package com.vikas.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vikas.model.GithubRepository;

/**
 * RepoDataRepository
 */
@Repository
public interface RepoDataRepository extends JpaRepository<GithubRepository, UUID> {

}
