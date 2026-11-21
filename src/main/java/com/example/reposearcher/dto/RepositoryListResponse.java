package com.example.reposearcher.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RepositoryListResponse(String message, List<RepositoryDto> repositories) {}
