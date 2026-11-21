package com.example.reposearcher.repository;

import com.example.reposearcher.model.GithubRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GithubRepositoryRepo
        extends JpaRepository<GithubRepository, Long>, JpaSpecificationExecutor<GithubRepository> {
}
