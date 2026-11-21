package com.example.reposearcher.controller;

import com.example.reposearcher.dto.*;
import com.example.reposearcher.service.RepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/github")
@Tag(name = "GitHub Repository Searcher")
public class GithubController {

    private final RepositoryService service;

    public GithubController(RepositoryService service) { this.service = service; }

    @Operation(summary = "Search GitHub repositories and store the results",
            description = "Calls the GitHub Search API, then upserts results into the database (existing repos are updated, not duplicated).")
    @ApiResponse(responseCode = "200", description = "Fetched and saved (or empty result list)")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "429", description = "GitHub rate limit hit")
    @ApiResponse(responseCode = "502", description = "GitHub unreachable / invalid response")
    @PostMapping("/search")
    public RepositoryListResponse search(@Valid @RequestBody SearchRequest request) {
        return service.searchAndSave(request);
    }

    @Operation(summary = "Retrieve stored repositories with optional filters")
    @GetMapping("/repositories")
    public RepositoryListResponse repositories(
            @Parameter(example = "Java") @RequestParam(required = false) String language,
            @Parameter(example = "100") @RequestParam(required = false) Integer minStars,
            @Parameter(description = "stars | forks | updated", example = "stars")
            @RequestParam(required = false, defaultValue = "stars") String sort) {
        return service.find(language, minStars, sort);
    }
}
