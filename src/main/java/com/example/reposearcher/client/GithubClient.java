package com.example.reposearcher.client;

import com.example.reposearcher.dto.RepositoryDto;
import com.example.reposearcher.exception.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class GithubClient {

    private final RestClient rest;

    public GithubClient(@Value("${github.api.base-url}") String baseUrl,
                        @Value("${github.api.token:}") String token) {
        RestClient.Builder b = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28");
        if (token != null && !token.isBlank()) {
            b.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        this.rest = b.build();
    }

    public List<RepositoryDto> search(String query, String language, String sort, int perPage) {
        StringBuilder q = new StringBuilder(query.trim());
        if (language != null && !language.isBlank()) q.append(" language:").append(language.trim());

        JsonNode body;
        try {
            body = rest.get()
                    .uri("/search/repositories?q={q}&sort={s}&order=desc&per_page={p}", q.toString(), sort, perPage)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException e) {
            throw mapError(e);
        } catch (RestClientException e) {
            throw new UpstreamException("Could not reach GitHub API", e);
        }

        if (body == null || !body.has("items") || !body.get("items").isArray()) {
            throw new UpstreamException("GitHub returned an invalid response");
        }

        List<RepositoryDto> out = new ArrayList<>();
        for (JsonNode n : body.get("items")) {
            if (!n.hasNonNull("id") || !n.hasNonNull("name")) continue; // skip malformed items
            out.add(new RepositoryDto(
                    n.get("id").asLong(),
                    n.get("name").asText(),
                    n.hasNonNull("description") ? truncate(n.get("description").asText(), 2000) : null,
                    n.path("owner").hasNonNull("login") ? n.get("owner").get("login").asText() : null,
                    n.hasNonNull("language") ? n.get("language").asText() : null,
                    n.path("stargazers_count").asInt(0),
                    n.path("forks_count").asInt(0),
                    n.hasNonNull("updated_at") ? Instant.parse(n.get("updated_at").asText()) : null));
        }
        return out;
    }

    private RuntimeException mapError(RestClientResponseException e) {
        int code = e.getStatusCode().value();
        HttpHeaders h = e.getResponseHeaders();
        boolean limited = code == 429
                || (code == 403 && h != null && "0".equals(h.getFirst("X-RateLimit-Remaining")))
                || (code == 403 && e.getResponseBodyAsString().toLowerCase().contains("rate limit"));
        if (limited) {
            String retry = h == null ? null : h.getFirst(HttpHeaders.RETRY_AFTER);
            if (retry == null && h != null) retry = h.getFirst("X-RateLimit-Reset"); // epoch seconds
            return new RateLimitException("GitHub API rate limit exceeded. Please retry later.", retry);
        }
        if (code == 422) return new InvalidRequestException("GitHub rejected the search query as invalid");
        return new UpstreamException("GitHub API error (HTTP " + code + ")", e);
    }

    private static String truncate(String s, int max) { return s.length() <= max ? s : s.substring(0, max); }
}
