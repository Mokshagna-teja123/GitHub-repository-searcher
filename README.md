# GitHub Repository Searcher

Spring Boot REST API that searches GitHub repositories, stores them in a database (upsert by GitHub repo id) and lets you query the stored data.

**Live URL:** `<your-deployed-url>`  |  **Swagger:** `<your-deployed-url>/swagger-ui.html`

## Endpoints
| Method | Path | Description |
|---|---|---|
| POST | `/api/github/search` | Body `{ "query": "springboot", "language": "Java", "sort": "stars" }`. Fetches from GitHub, upserts into DB. |
| GET | `/api/github/repositories?language=Java&minStars=100&sort=stars` | Reads stored repos. All params optional; sort = `stars` (default) / `forks` / `updated`. |

## Edge cases handled
- Existing repo -> updated (primary key = GitHub repo id), never duplicated
- Empty GitHub result -> `200` with message "No repositories found..." and `[]`
- GitHub rate limit (403/429) -> `429` + `Retry-After`
- Invalid/malformed GitHub response or network failure -> `502`
- Invalid input (blank query, bad sort, bad JSON, negative minStars) -> `400`
- Null descriptions / languages / owners handled safely

## Run locally
```bash
mvn spring-boot:run          # H2 in-memory DB
# open http://localhost:8080/swagger-ui.html
```
Optional: `export GITHUB_TOKEN=ghp_xxx` to raise GitHub's rate limit.

## Deploy (Render + free Postgres)
1. Push to GitHub (repo name: `github-repository-searcher`).
2. Render -> New **PostgreSQL** (free) -> note host, db, user, password.
3. Render -> New **Web Service** -> connect repo -> Runtime **Docker**.
4. Environment variables:
   - `SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/<db>` (must start with `jdbc:postgresql://`)
   - `SPRING_DATASOURCE_USERNAME=<user>`
   - `SPRING_DATASOURCE_PASSWORD=<password>`
   - `GITHUB_TOKEN=<optional>`
5. Deploy, then open `<url>/swagger-ui.html`. (Free tier sleeps; hit the URL once before recording.)

## Design decisions
1. **GitHub repo id as primary key**: `saveAll` becomes a natural upsert, with no race-prone "exists?" checks.
2. **JPA Specifications** for optional filters: one query, no combinatorial repository methods.
