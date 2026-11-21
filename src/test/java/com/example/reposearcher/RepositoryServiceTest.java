package com.example.reposearcher;

import com.example.reposearcher.client.GithubClient;
import com.example.reposearcher.dto.*;
import com.example.reposearcher.repository.GithubRepositoryRepo;
import com.example.reposearcher.service.RepositoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class RepositoryServiceTest {

    @Autowired RepositoryService service;
    @Autowired GithubRepositoryRepo repo;
    @MockBean GithubClient client;

    @Test
    void existingRepoIsUpdatedNotDuplicated() {
        SearchRequest req = new SearchRequest();
        req.setQuery("spring");
        when(client.search(anyString(), any(), anyString(), anyInt())).thenReturn(
                List.of(new RepositoryDto(1L, "demo", "d", "me", "Java", 10, 1, Instant.now())));
        service.searchAndSave(req);

        when(client.search(anyString(), any(), anyString(), anyInt())).thenReturn(
                List.of(new RepositoryDto(1L, "demo", "d", "me", "Java", 99, 1, Instant.now())));
        service.searchAndSave(req);

        assertEquals(1, repo.count());
        assertEquals(99, repo.findById(1L).orElseThrow().getStars());
    }

    @Test
    void filtersByLanguageAndMinStars() {
        SearchRequest req = new SearchRequest();
        req.setQuery("x");
        when(client.search(anyString(), any(), anyString(), anyInt())).thenReturn(List.of(
                new RepositoryDto(2L, "a", null, "o", "Go", 5, 0, Instant.now()),
                new RepositoryDto(3L, "b", null, "o", "Go", 500, 0, Instant.now())));
        service.searchAndSave(req);
        assertEquals(2, service.find("go", null, "stars").repositories().stream().filter(r -> r.id() >= 2).count());
        assertEquals(1, service.find("go", 100, "stars").repositories().stream().filter(r -> r.id() >= 2).count());
    }
}
