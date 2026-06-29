---
name: eco-knock-maintainer
description: Maintain eco-knock-be-central code in the repository's established style. Use when modifying, reviewing, refactoring, testing, or organizing project code, especially around layered architecture, airquality CQRS boundaries, Spring Boot/Kotlin/Java style, controllers, services, repositories, Flyway migrations, and E2E tests.
---

# Eco Knock Maintainer

Use this skill when working on application code in `eco-knock-be-central`.

## Core Principles

1. Prioritize readability over architectural complexity.
2. Keep changes close to the current code style before introducing new abstractions.
3. Prefer layered architecture across the project.
4. Treat `airquality` as a layered module with light clean-architecture influence:
   - controllers depend on usecases
   - usecases are interfaces
   - services implement usecases
   - command and query services stay separate
   - write model and read model stay separate
5. Treat `overview` as a simple layered module:
   - controllers call `OverviewService`
   - services own transaction boundaries
   - repositories stay behind services
   - request DTOs validate request-specific rules
6. Do not introduce heavy architecture ceremony unless it clearly improves readability.

## Before Editing

1. Read the nearby code first.
2. Check package names, DTO shapes, exception style, and test style before deciding.
3. Preserve user changes in dirty files.
4. Prefer the smallest edit that makes the behavior clear.
5. Ask before large package moves, broad rewrites, or architecture changes with multiple valid directions.

## Architecture Guidance

Use layered architecture as the default:

- `controller`: HTTP/SSE entry points
- `usecase`: application-facing interfaces
- `service`: usecase implementations and transaction boundaries
- `repository`: persistence access
- `model/entity`: write model
- `readmodel/entity`: read model backed by views/materialized views
- `dto/request`: inbound request DTOs
- `dto/response`: outbound response DTOs
- `exception`: domain-specific client exceptions

For `airquality`:

- Keep command writes through `AirQualityCommandService` and `AirQualityRepository`.
- Keep query reads through `AirQualityQueryService` and read-model repositories.
- Do not make query code aggregate from raw `air_quality` when a read model exists.
- Keep materialized-view refresh logic outside request handlers.
- Treat SSE publish as a post-save notification, not the write model itself.

Typical flow:

```text
HTTP request
-> controller
-> usecase interface
-> service implementation
-> repository
-> entity/readmodel
```

Command flow:

```text
AirQualityConsumer
-> SaveAirQualityUseCase
-> AirQualityCommandService
-> AirQualityRepository
-> AirQuality
```

Query flow:

```text
AirQualityTimeseriesController
-> QueryAirQualityUseCase
-> AirQualityQueryService
-> AirQuality*ViewRepository
-> AirQuality*View
-> AirQualityTimeseriesPointResponse
```

Avoid:

- controller calling repository directly
- query service using `AirQualityRepository` to aggregate raw rows when a read model exists
- command service depending on read-model repositories
- request handlers refreshing materialized views
- adding a generic helper when an explicit branch is easier to read

For `overview`:

- Keep user shortcut rows independent from default shortcut rows after copying.
- Use `default_overview_shortcut` only as the reset/init source.
- Validate full shortcut replacement requests in `UpdateOverviewShortcutRequest`.
- Require `sortOrder` to contain exactly `0..n-1` without duplicates.
- Query user shortcuts ordered by `sortOrder`.
- Keep URL validation in the `ValidHttpUrl` value object and DTO binding path.

For `auth` and `sso`:

- Keep first-party token issuing, refresh, token DTOs, auth policy config, and auth client exceptions under `auth`.
- Keep auth-econovation integration details under `sso`: SSO `/me` client, WEB callback handling, redirect URL resolving, and SSO-specific client exceptions.
- Treat auth-econovation `role` as separate from this service's `Member.role`; do not promote internal roles from the SSO role unless the user explicitly changes that policy.
- Store only refresh token `jti` values in Redis, keyed by `auth:refresh:{memberId}`. Do not store raw refresh token strings.
- Keep refresh token comparison and replacement atomic. Use the Lua script resource under `src/main/resources/redis` for compare-and-replace behavior instead of splitting Redis `GET` and `SET` in service code.
- Prefer repository method names that describe the Redis operation directly, such as `replaceIfMatches`, and translate Lua return codes into a small enum before they reach services.
- Keep Spring Security infrastructure under `common.security`: filters, handlers, user details, policy resolver, and JWT helper/util wiring.
- Do not replace `JwtAuthHelper`'s existing filter-level `UnauthorizedException` flow with auth domain `ClientException`; auth domain `ClientException` is for controller/service API failures such as refresh token reissue.
- For redirect parameters that eventually feed `sendRedirect`, validate against the frontend allowlist both when accepting the query parameter and before final redirect from a stored cookie.

For `whozin`:

- Keep Whozin external API integration under `whozin`: HTTP client, config, external response DTOs, internal DTO conversion, and service facade.
- Treat the actual Whozin response as the source of truth over Swagger examples when they differ.
- Preserve snake_case response mapping with Jackson naming annotations and keep `generated_at` timezone handling aligned with the real response shape.
- Parse Korean `presence_duration` strings explicitly, and fail loudly with `InternalServerException` when the upstream response shape is unexpected.
- Tests that call the real Whozin API must make the external dependency obvious and require `WHOZIN_TOKEN`; do not silently replace them with mocks unless the user asks.

For deployment config:

- Keep shared Spring settings in `application.yaml` and environment-specific DB/Redis settings in `application-dev.yaml` or `application-prod.yaml`.
- Keep local dev compose under `deploy/dev` and production compose/scripts under `deploy/prod`.
- Production compose should run the Spring app with `SPRING_PROFILES_ACTIVE=prod` and use compose service names for internal Postgres/Redis hosts.
- Keep compose wrapper scripts thin: they should delegate to `docker compose` with the repository root `.env` and avoid duplicating compose configuration.

## Code Style

Match the existing style unless there is a clear reason not to.

- Put Kotlin `companion object` near the top of the class body.
- Keep Kotlin classes concise and constructor-injected.
- Prefer explicit domain names over generic names.
- Prefer simple `when` branches over clever maps when the explicit form is easier to read.
- Avoid silent request correction. Invalid client input should throw a domain exception.
- Prefer domain exceptions under the owning package over generic `IllegalArgumentException`.
- Keep validation close to request DTOs when the rule is request-specific.
- Keep transaction annotations on services, not controllers.
- Keep comments rare; delete stale TODOs instead of preserving outdated notes.
- Use Java static factories or small Kotlin extension functions when Java Lombok builders make Kotlin call sites noisy; avoid adding broad converter classes for one-off simple transformations.

Kotlin class shape:

```kotlin
@Service
@Transactional(readOnly = true)
class SomeQueryService(
    private val repository: SomeRepository,
) : SomeUseCase {
    companion object {
        private const val DEFAULT_LIMIT = 100
    }

    override fun query(request: SomeRequest): SomeResponse {
        // keep the flow direct and readable
    }
}
```

Request validation shape:

```kotlin
data class SomeRequest(
    val limit: Int,
) {
    init {
        if (limit !in MIN_LIMIT..MAX_LIMIT) {
            throw SomeDomainException()
        }
    }

    companion object {
        const val MIN_LIMIT = 1
        const val MAX_LIMIT = 500
    }
}
```

Exception shape:

```kotlin
class BadSomethingException : ClientException(ErrorCode.BAD_SOMETHING)
```

Avoid silently fixing invalid requests:

```kotlin
// Avoid
val limit = request.limit.coerceIn(1, 500)

// Prefer
if (limit !in 1..500) throw BadLimitException()
```

## API And DTO Guidance

- Return simple response shapes that frontend code can consume directly.
- Wrap controller JSON responses in `CommonResponse.success(...)`, `CommonResponse.emptySuccess()`, or `CommonResponse.failure(...)` unless an endpoint intentionally streams raw data.
- Use Spring Data `Slice` directly when the controller naturally returns a slice.
- For cursor-style history reads, `last == false` means more historical data exists.
- Use `OffsetDateTime` at API boundaries when clients send timezone-aware timestamps.
- Convert to `Instant` inside service/repository code.
- For enum request values with custom codes, support Jackson body binding with `@JsonCreator` and `@JsonValue`.
- For timeseries `GET` handlers, bind request DTOs from query parameters with `@ModelAttribute` or no annotation; do not use JSON bodies unless explicitly requested.
- SSE endpoints return `SseEmitter` directly. Event payloads may still use `CommonResponse.success(...)` when sending application data.

Enum code shape:

```kotlin
enum class SomeResolution(
    @get:JsonValue
    val code: String,
) {
    FIVE_MINUTES("5m");

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromOrThrowBusinessException(code: String): SomeResolution {
            return entries.find { it.code == code } ?: throw BadResolutionException()
        }
    }
}
```

Value object URL binding shape:

```java
@Embeddable
public record ValidHttpUrl(String value) {
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public ValidHttpUrl {
        // validate syntax and allowed scheme
    }

    @JsonValue
    @Override
    public String value() {
        return value;
    }
}
```

## Persistence And Migration Guidance

- This project uses Flyway with `spring.jpa.hibernate.ddl-auto=validate`; every new entity/table/column needs a matching migration.
- Keep migration filenames monotonic: `V{next}__short_description.sql`.
- Do not edit already-applied Flyway migrations such as `V1__init_schema.sql`; add the next versioned migration instead.
- Match Hibernate's current physical naming style in SQL: `overview_shortcut`, `member_id`, `sort_order`.
- Add indexes for query patterns that are part of the service contract, such as `(member_id, sort_order)` for ordered overview shortcut reads.
- Prefer explicit foreign keys for entity relationships.

## Testing Guidance

- Follow the project's existing E2E style:
  - `@SpringBootTest(webEnvironment = RANDOM_PORT)`
  - constructor injection
  - `RestClient` for real HTTP calls
  - no mocks unless the user explicitly allows them
- Prefer testing real controller/service/repository/Flyway behavior when the request says E2E.
- If tests use the development DB, isolate data by a clearly bounded test timestamp range and clean it up.
- Verify invalid request cases return the expected domain error code.
- Run the narrowest relevant Gradle task first, then broader verification when feasible.

E2E test shape:

```kotlin
@SpringBootTest(
    classes = [EcoKnockBeCentralApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class SomeControllerE2ETest(
    @param:LocalServerPort
    private val port: Int,
    private val jdbcTemplate: JdbcTemplate,
    private val mapper: ObjectMapper,
) {
    private val restClient = RestClient.builder()
        .baseUrl("http://localhost:$port")
        .build()
}
```

Avoid:

- replacing E2E collaborators with mocks without explicit user approval
- deleting broad development data in tests
- asserting undocumented JSON fields when Spring Data serialization already provides a stable field such as `content` or `last`

## Boundaries With Other Skills

- Use `readme-maintainer` for README updates.
- Use `git-commit-korean` for commit creation or Korean commit message work.
- This skill is for code maintenance, architecture consistency, style, and tests.
