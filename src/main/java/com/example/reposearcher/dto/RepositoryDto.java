package com.example.reposearcher.dto;

import com.example.reposearcher.model.GithubRepository;
import java.time.Instant;

public record RepositoryDto(Long id, String name, String description, String owner,
                            String language, int stars, int forks, Instant lastUpdated) {

    public static RepositoryDto from(GithubRepository r) {
        return new RepositoryDto(r.getId(), r.getName(), r.getDescription(), r.getOwner(),
                r.getLanguage(), r.getStars(), r.getForks(), r.getLastUpdated());
    }

    public GithubRepository toEntity() {
        return new GithubRepository(id, name, description, owner, language, stars, forks, lastUpdated);
    }
}
