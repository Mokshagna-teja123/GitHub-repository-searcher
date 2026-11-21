package com.example.reposearcher.service;

import com.example.reposearcher.client.GithubClient;
import com.example.reposearcher.dto.*;
import com.example.reposearcher.exception.InvalidRequestException;
import com.example.reposearcher.model.GithubRepository;
import com.example.reposearcher.repository.GithubRepositoryRepo;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RepositoryService {

    private final GithubClient client;
    private final GithubRepositoryRepo repo;

    public RepositoryService(GithubClient client, GithubRepositoryRepo repo) {
        this.client = client;
        this.repo = repo;
    }

    /** Fetch from GitHub, upsert by GitHub id (existing rows updated, new rows inserted). */
    @Transactional
    public RepositoryListResponse searchAndSave(SearchRequest req) {
        String sort = req.getSort() == null ? "stars" : req.getSort().toLowerCase();
        int perPage = req.getPerPage() == null ? 30 : req.getPerPage();

        List<RepositoryDto> fetched = client.search(req.getQuery(), req.getLanguage(), sort, perPage);
        if (fetched.isEmpty()) {
            return new RepositoryListResponse("No repositories found for the given search", List.of());
        }

        // de-duplicate within the batch, then save (save() on an existing id = UPDATE)
        Map<Long, GithubRepository> unique = new LinkedHashMap<>();
        fetched.forEach(d -> unique.put(d.id(), d.toEntity()));
        repo.saveAll(unique.values());

        return new RepositoryListResponse("Repositories fetched and saved successfully", fetched);
    }

    @Transactional(readOnly = true)
    public RepositoryListResponse find(String language, Integer minStars, String sort) {
        if (minStars != null && minStars < 0) throw new InvalidRequestException("minStars must be >= 0");

        String field = switch (sort == null ? "stars" : sort.toLowerCase()) {
            case "stars" -> "stars";
            case "forks" -> "forks";
            case "updated" -> "lastUpdated";
            default -> throw new InvalidRequestException("sort must be one of: stars, forks, updated");
        };

        Specification<GithubRepository> spec = (root, q, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (language != null && !language.isBlank())
                p.add(cb.equal(cb.lower(root.get("language")), language.trim().toLowerCase()));
            if (minStars != null)
                p.add(cb.greaterThanOrEqualTo(root.get("stars"), minStars));
            return cb.and(p.toArray(new Predicate[0]));
        };

        List<RepositoryDto> list = repo.findAll(spec, Sort.by(Sort.Direction.DESC, field)).stream()
                .map(RepositoryDto::from).toList();
        return new RepositoryListResponse(null, list);
    }
}
