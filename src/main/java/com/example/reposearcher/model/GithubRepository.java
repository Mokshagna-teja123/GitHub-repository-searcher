package com.example.reposearcher.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "github_repositories", indexes = {
        @Index(name = "idx_repo_language", columnList = "language"),
        @Index(name = "idx_repo_stars", columnList = "stars")
})
public class GithubRepository {

    /** GitHub's own repository id: unique, so saving an existing id updates instead of duplicating. */
    @Id
    private Long id;
    private String name;
    @Column(length = 2000)
    private String description;
    private String owner;
    private String language;
    private int stars;
    private int forks;
    private Instant lastUpdated;

    public GithubRepository() {}

    public GithubRepository(Long id, String name, String description, String owner,
                            String language, int stars, int forks, Instant lastUpdated) {
        this.id = id; this.name = name; this.description = description; this.owner = owner;
        this.language = language; this.stars = stars; this.forks = forks; this.lastUpdated = lastUpdated;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getOwner() { return owner; }
    public String getLanguage() { return language; }
    public int getStars() { return stars; }
    public int getForks() { return forks; }
    public Instant getLastUpdated() { return lastUpdated; }
}
