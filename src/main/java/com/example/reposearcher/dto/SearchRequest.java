package com.example.reposearcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public class SearchRequest {
    @NotBlank(message = "query must not be blank")
    @Schema(example = "springboot")
    private String query;

    @Schema(example = "Java", description = "Optional programming language filter")
    private String language;

    @Pattern(regexp = "(?i)stars|forks|updated", message = "sort must be one of: stars, forks, updated")
    @Schema(example = "stars", description = "stars | forks | updated (default stars)")
    private String sort;

    @Min(1) @Max(100)
    @Schema(example = "30", description = "Results to fetch from GitHub, 1-100 (default 30)")
    private Integer perPage;

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
    public Integer getPerPage() { return perPage; }
    public void setPerPage(Integer perPage) { this.perPage = perPage; }
}
