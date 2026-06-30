---
name: api-doc-maintainer
description: Keep this Spring Boot project's Scalar/OpenAPI documentation accurate when REST or SSE endpoints, controller annotations, request/response DTOs, status codes, ErrorCode entries, auth/security exposure, or API examples are added or changed. Use when adding a new endpoint, changing an existing endpoint contract, reviewing API docs, fixing Scalar or /v3/api-docs output, or updating OpenAPI annotations.
---

# API Doc Maintainer

Use this skill when endpoint behavior or API documentation can change.

## Core Rules

1. Treat the generated `/v3/api-docs` output as the source of truth for Scalar, not just the annotations.
2. Document only implemented HTTP/SSE APIs. Do not put gRPC proto methods into OpenAPI unless they are exposed through HTTP.
3. Keep controller examples tied to `OpenApiConfig` reusable examples and `OpenApiConstants` name/ref constants.
4. Do not hardcode raw JSON response examples directly in controller annotations.
5. Keep admin Thymeleaf or HTML screens hidden with `@Hidden`; expose only JSON admin APIs.

## Endpoint Documentation Checklist

For every new or changed endpoint:

1. Add or update `@Tag` and `@Operation` with Korean summaries/descriptions that match the real behavior.
2. Add `@ApiResponse` entries for the actual success, redirect, stream, auth, validation, and domain-error statuses.
3. For JSON endpoints, set `produces = [MediaType.APPLICATION_JSON_VALUE]`.
4. For endpoints with JSON request bodies, set `consumes = [MediaType.APPLICATION_JSON_VALUE]`.
5. For redirect endpoints, document the real redirect status such as `302` and do not call it `200 OK`.
6. For SSE endpoints, use `produces = [MediaType.TEXT_EVENT_STREAM_VALUE]` and document `text/event-stream`.
7. For authenticated endpoints, document `401` with the `Unauthorized` example unless the endpoint has a more specific auth error.
8. For request syntax and semantic validation, split `400` and `422` when the implementation does.

## Parameters And DTOs

1. For GET query DTOs, make Scalar show each query parameter separately.
   - If `@ParameterObject` renders as a single `request object`, hide the DTO parameter and document each query parameter explicitly with `@Parameter`.
   - Include `required`, `description`, `schema`, allowed enum values, min/max, and examples when relevant.
2. For request body DTOs, prefer DTO-level `@Schema` annotations for fields that need examples, min/max, or business constraints.
3. Keep documented field names aligned with Jackson binding, including custom enum codes and value objects.

## Error Examples

1. Reuse `OpenApiConfig` component examples created from `CommonResponse.emptySuccess()` and `CommonResponse.failure(ErrorCode...)`.
2. Add new examples by registering them in `OpenApiConfig` and adding matching name/ref constants in `OpenApiConstants`.
3. In controller annotations, reference examples with both `name` and `ref`.
4. Never leave generated response content as `"example": null`.
5. Prefer concrete examples for common cases:
   - auth failure: `SECURITY_401_001`, `AUTH_401_002`, or SSO-specific errors
   - request syntax failure: `COMMON_400_002`
   - request meaning failure: `COMMON_422_001`
   - domain validation failure: the owning domain `ErrorCode`

## Security And Exposure

1. If adding public docs paths, keep `SecurityConfig` and `AuthPolicyResolver` aligned.
2. Keep `/scalar`, `/scalar/**`, `/v3/api-docs`, `/v3/api-docs/**`, and `/v3/api-docs.yaml` routed through `ApiDocAccessFilter`.
3. Keep API docs exposure controlled only by Redis key `admin:api-docs:enabled`; missing Redis key or Redis lookup failure means disabled.
4. Do not disable Springdoc or Scalar with profile properties if runtime admin toggling is expected.
5. Verify authenticated business APIs still require authentication at runtime; documentation access rules must not weaken API auth.

## Verification Workflow

1. Run the narrow compile check:

```powershell
.\gradlew.bat classes
```

2. When practical, start the app on a temporary port such as `18082` and fetch `/v3/api-docs`.
3. Inspect the generated JSON for:
   - expected path and method entries
   - no unintended admin HTML paths
   - query parameters shown as individual fields, not a single `request object`
   - no `example: null` under `application/json`
   - expected component examples present
   - JSON endpoints using `application/json`
   - SSE endpoints using `text/event-stream`
   - docs paths returning `404` when Redis toggle is missing or false
4. Run the narrowest relevant tests first, then broader tests if the local DB and external services are available.
5. If full tests cannot run because Postgres, Redis, or gRPC dependencies are unavailable, report that clearly.
