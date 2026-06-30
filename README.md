# eco-knock-be-central

`eco-knock-be-central`은 임베디드 장치의 센서/공기청정기 gRPC 데이터를 수집하고, 공기질 데이터를 저장·조회하는 Spring Boot 기반 중앙 백엔드입니다.

현재 구현은 PostgreSQL 저장소, Redis 기반 refresh token 및 API 문서 공개 상태 저장, Flyway 스키마 관리, SSO 기반 로그인, HttpOnly 쿠키 기반 JWT 인증, 공기질 timeseries 조회 API, SSE 실시간 스트림, overview shortcut API, 관리자 SSR 화면, Scalar/OpenAPI 문서, Whozin 공개 회원 조회 연동, Actuator/Prometheus 메트릭 엔드포인트를 포함합니다.

## 현재 구현 범위

- Spring Boot 4 + Java 25 + Kotlin 혼합 프로젝트
- PostgreSQL + Spring Data JPA
- Redis 기반 refresh token jti 저장
- Flyway 기반 DB 스키마 및 materialized view 관리
- `air_quality` 원본 데이터 저장
- `1m`, `5m`, `15m`, `1h`, `4h`, `1d` 공기질 materialized view
- 매분 materialized view refresh scheduler
- 공기질 timeseries 조회 API
- 공기질 과거 데이터 history 조회 API
- 공기질 SSE 실시간 스트림
- 사용자 overview shortcut 조회·수정·기본값 재설정 API
- 관리자용 default overview shortcut SSR 조회·저장 화면
- 관리자용 API 문서 공개/비공개 토글
- Springdoc OpenAPI 및 Scalar API 문서
- Whozin 공개 회원 조회 API 연동 및 내부 DTO 변환
- 센서/공기청정기 현재 상태 gRPC polling producer
- queue 기반 공기질 저장 consumer
- auth-econovation WEB SSO 로그인/콜백
- 프로젝트 `Member.role = ADMIN` 기반 관리자 권한
- 관리자 마스터 비밀번호 기반 1시간짜리 전용 JWT 쿠키 인증
- 자체 access/refresh JWT HttpOnly 쿠키 발급 및 재발급
- Redis Lua script 기반 refresh token 재사용 탐지
- Redis 기반 API 문서 공개 상태 저장
- JWT 쿠키 인증 필터 및 optional 인증 정책
- Actuator health/info/prometheus 엔드포인트
- proto 기반 코드 생성 Gradle 설정

## 아직 미완성인 부분

- gRPC 서버가 떠 있지 않으면 producer가 연결 실패 로그를 남깁니다.

## 기술 스택

- Java 25
- Kotlin
- Spring Boot 4
- Spring Security
- Thymeleaf
- Spring Data JPA
- Spring Data Redis
- PostgreSQL
- Redis
- Flyway
- Springdoc OpenAPI
- Scalar
- gRPC / Protocol Buffers
- Actuator / Micrometer Prometheus
- JJWT
- Gradle

## 프로젝트 구조

```text
src/main/java/jnu/econovation/ecoknockbecentral
├─ airquality
│  ├─ model/entity
│  └─ readmodel/entity
├─ common
│  ├─ exception
│  ├─ security
│  ├─ converter
│  ├─ dto/response
│  └─ util
├─ member
│  └─ model
└─ overview
   ├─ model/entity
   └─ model/vo

src/main/kotlin/jnu/econovation/ecoknockbecentral
├─ admin
│  └─ controller
├─ airquality
│  ├─ controller
│  ├─ dto
│  ├─ messaging
│  ├─ queue
│  ├─ repository
│  ├─ scheduler
│  ├─ service
│  └─ usecase
├─ auth
│  ├─ config
│  ├─ constant
│  ├─ controller
│  ├─ dto
│  ├─ exception
│  ├─ repository
│  └─ service
├─ common
│  ├─ extension
│  └─ openapi
├─ grpc
│  ├─ client
│  └─ config
├─ member
├─ overview
│  ├─ controller
│  ├─ dto
│  ├─ extension
│  ├─ repository
│  └─ service
├─ whozin
│  ├─ client
│  ├─ config
│  ├─ dto
│  └─ service
└─ sso
   ├─ client
   ├─ config
   ├─ constant
   ├─ controller
   ├─ dto
   ├─ exception
   ├─ resolver
   └─ service

src/main/resources/db/migration
├─ V1__init_schema.sql
├─ V2__create_air_quality_materialized_views.sql
├─ V3__add_air_quality_materialized_view_resolutions.sql
├─ V4__recreate_air_quality_materialized_views_with_double_precision.sql
├─ V5__create_light_report.sql
├─ V6__create_overview_shortcuts.sql
└─ V7__update_member_for_sso.sql

src/main/resources/redis
└─ rotate-refresh-token.lua

src/main/resources/templates/admin
├─ access-denied.html
├─ index.html
├─ login.html
└─ overview-shortcuts.html

src/main/resources/static/admin
├─ access-denied.css
├─ api-docs-access.js
├─ admin.css
├─ login.css
├─ overview-shortcuts.css
└─ overview-shortcuts.js

src/main/proto
├─ sensor/v1/sensor.proto
├─ sensor/v2/sensor.proto
└─ airpurifier/v1/airpurifier.proto
```

## 실행 전 요구사항

- JDK 25
- PostgreSQL
- Redis
- `sensor.v2.SensorService`와 `airpurifier.v1.AirPurifierService`를 제공하는 gRPC 서버

기본 설정:

- HTTP 서버 포트: `18081`
- PostgreSQL: `localhost:5432`, 데이터베이스명 `ecoknock`
- Redis: `localhost:6379`
- gRPC 서버: `localhost:6565`

## 환경 변수

이 프로젝트는 개발 환경에서 `springboot4-dotenv`를 사용하므로 루트 `.env` 파일 또는 시스템 환경 변수로 값을 주입할 수 있습니다.

설정 파일은 공통 `application.yaml`과 profile별 `application-dev.yaml`, `application-prod.yaml`로 분리되어 있습니다. 별도 profile을 지정하지 않으면 `dev` profile이 기본으로 적용됩니다.

필수:

- `JWT_SECRET_KEY`
  - JWT 서명 키입니다.
  - `Keys.hmacShaKeyFor(...)`에 사용할 만큼 충분히 긴 문자열이어야 합니다.
- `AES256_KEY`
  - 정확히 32바이트 문자열이어야 합니다.
- `SSO_CLIENT_ID`
  - auth-econovation에 등록된 WEB client id입니다.
- `WHOZIN_TOKEN`
  - Whozin 공개 회원 API 호출에 사용하는 Bearer token입니다.
- `ADMIN_MASTER_PASSWORD`
  - 관리자 화면의 마스터 비밀번호 로그인에 사용하는 평문 비밀번호입니다.
  - 이 값이 없으면 애플리케이션 시작 시 설정 바인딩이 실패해야 합니다.

선택:

- `DEV_POSTGRES_HOST` 기본값 `localhost`
- `DEV_POSTGRES_PORT` 기본값 `5432`
- `DEV_POSTGRES_USERNAME` 기본값 `postgres`
- `DEV_POSTGRES_PASSWORD` 기본값 빈 문자열
- `DEV_REDIS_HOST` 기본값 `localhost`
- `DEV_REDIS_PORT` 기본값 `6379`
- `PROD_POSTGRES_HOST` 기본값 `postgres`
- `PROD_POSTGRES_PORT` 기본값 `5432`
- `PROD_POSTGRES_USERNAME`
- `PROD_POSTGRES_PASSWORD`
- `PROD_REDIS_HOST` 기본값 `redis`
- `PROD_REDIS_PORT` 기본값 `6379`
- `PROD_SERVER_PORT` 기본값 `18081`

예시:

```dotenv
JWT_SECRET_KEY=replace-with-a-long-secret-key
AES256_KEY=12345678901234567890123456789012
SSO_CLIENT_ID=replace-with-sso-client-id
WHOZIN_TOKEN=replace-with-whozin-token
ADMIN_MASTER_PASSWORD=replace-with-admin-master-password
DEV_POSTGRES_HOST=localhost
DEV_POSTGRES_PORT=5432
DEV_POSTGRES_USERNAME=postgres
DEV_POSTGRES_PASSWORD=postgres
DEV_REDIS_HOST=localhost
DEV_REDIS_PORT=6379
PROD_POSTGRES_USERNAME=postgres
PROD_POSTGRES_PASSWORD=replace-with-prod-password
```

## 실행 방법

proto 생성은 `compileJava`, `compileKotlin`, `bootRun` 전에 자동으로 연결되어 있습니다.

로컬 개발용 PostgreSQL/Redis 실행:

```powershell
.\deploy\dev\dev.ps1
```

자주 쓰는 Docker Compose 명령은 첫 번째 인자로 넘길 수 있습니다.

```powershell
.\deploy\dev\dev.ps1 ps
.\deploy\dev\dev.ps1 logs
.\deploy\dev\dev.ps1 down
```

Git Bash 또는 Unix 계열 셸에서는 다음 스크립트를 사용할 수 있습니다.

```bash
sh ./deploy/dev/dev.sh
sh ./deploy/dev/dev.sh ps
sh ./deploy/dev/dev.sh down
```

운영 compose 설정은 Spring Boot 애플리케이션, PostgreSQL, Redis를 함께 실행합니다. 운영 profile은 `prod`로 고정되며 루트 `.env`의 `PROD_...` 값과 공통 secret 값을 사용합니다.

```powershell
.\deploy\prod\prod.ps1
.\deploy\prod\prod.ps1 logs
.\deploy\prod\prod.ps1 down
```

Git Bash 또는 Unix 계열 셸에서는 다음 스크립트를 사용할 수 있습니다.

```bash
sh ./deploy/prod/prod.sh
sh ./deploy/prod/prod.sh logs
sh ./deploy/prod/prod.sh down
```

운영 서버에서 최신 `main`을 당긴 뒤 compose를 실행하려면 배포 스크립트를 사용합니다.

```powershell
.\deploy\prod\deploy.ps1
```

```bash
sh ./deploy/prod/deploy.sh
```

로컬 실행:

```bash
./gradlew bootRun
```

Windows PowerShell:

```powershell
.\gradlew.bat bootRun
```

테스트 실행:

```bash
./gradlew test
```

테스트는 현재 개발 DB 설정과 필수 환경 변수를 사용합니다. `JWT_SECRET_KEY`, `AES256_KEY`, `SSO_CLIENT_ID`, `WHOZIN_TOKEN`, `ADMIN_MASTER_PASSWORD`가 없으면 테스트도 애플리케이션과 동일하게 설정 바인딩에 실패합니다.

E2E 테스트는 `2099-01-01T00:00:00Z`부터 `2099-01-01T00:10:00Z` 전까지의 테스트 데이터를 삽입/삭제하거나, 테스트용 SSO member id 범위의 데이터를 정리합니다.

Whozin 실제 API 조회 테스트는 `WHOZIN_TOKEN`과 외부 네트워크에 의존합니다.

```bash
./gradlew test --tests "jnu.econovation.ecoknockbecentral.whozin.service.WhozinServiceTest"
```

## proto 생성

수동으로 proto 생성만 실행하려면:

```bash
./gradlew generateProto
```

생성 대상 proto:

- [`sensor.proto`](./src/main/proto/sensor/v1/sensor.proto)
- [`sensor.v2.proto`](./src/main/proto/sensor/v2/sensor.proto)
- [`airpurifier.proto`](./src/main/proto/airpurifier/v1/airpurifier.proto)

## DB 마이그레이션

Flyway는 `classpath:db/migration` 아래 SQL을 실행합니다.

- `V1`: `air_quality`, `member` 테이블 생성
- `V2`: `5m`, `15m`, `1h` 공기질 materialized view 생성
- `V3`: `1m`, `4h`, `1d` 공기질 materialized view 추가
- `V4`: 공기질 materialized view를 `avg_pm25 double precision` 기준으로 재생성
- `V5`: 조도 리포트 저장용 `light_report` 테이블 생성
- `V6`: overview shortcut, default overview shortcut 테이블 생성
- `V7`: member에 SSO 식별자 컬럼 추가 및 OAuth2 provider 컬럼 제거

JPA 설정은 `ddl-auto: validate`이므로 애플리케이션 시작 시 엔티티와 DB 스키마가 맞는지 검증합니다.

## 공기질 수집 흐름

1. `AirQualityProducer`가 앱 시작 후 센서/공기청정기 gRPC 서버를 polling합니다.
2. 조회한 원시 데이터를 `AirQualityDTO`로 병합합니다.
3. `SaveAirQualityQueue`에 저장 명령을 넣습니다.
4. `AirQualityConsumer`가 명령을 소비해 `air_quality` 테이블에 저장합니다.
5. 저장 후 SSE 구독자에게 최신 데이터를 발행합니다.

gRPC 조회 실패 시 producer는 백오프 delay를 적용합니다.

## API 문서

상세 API 명세는 README에 직접 적지 않고 Springdoc OpenAPI와 Scalar UI로 확인합니다.

- Scalar UI: `/scalar`
- OpenAPI JSON: `/v3/api-docs`
- OpenAPI YAML: `/v3/api-docs.yaml`

API 문서 경로는 Spring Security에서 접근 가능한 경로로 등록되어 있지만, 최종 노출 여부는 `ApiDocAccessFilter`가 Redis 값으로 제어합니다.

```text
key   = admin:api-docs:enabled
value = true | false
```

Redis 값이 `"true"`일 때만 문서가 열립니다. Redis key가 없거나 Redis 조회에 실패하면 문서 경로는 `404`로 응답합니다. 이 값은 관리자 화면에서 활성화하거나 비활성화할 수 있습니다.

## API 기능 요약

- 공기질 timeseries 범위 조회와 history 조회는 materialized view를 기반으로 합니다.
- 공기질 SSE 스트림은 저장된 최신 공기질 데이터를 실시간으로 발행합니다.
- 인증은 auth-econovation WEB SSO와 자체 HttpOnly JWT 쿠키를 함께 사용합니다.
- refresh token은 JWT 원문 대신 `jti`만 Redis에 저장하며, 재발급 시 Lua script로 현재 jti 비교와 새 jti 저장을 원자적으로 처리합니다.
- overview shortcut API는 인증된 사용자의 바로가기 목록 조회, 전체 교체, 기본값 재설정을 제공합니다.
- Whozin 연동은 공개 회원 API 응답을 내부 DTO로 변환해 사용합니다.

## 관리자 화면

관리자 화면은 Spring Boot 서버가 Thymeleaf로 렌더링합니다. 별도 프론트엔드 앱은 없습니다.

관리자 로그인은 두 가지 방식을 지원합니다.

- SSO 로그인: 기존 auth-econovation SSO를 사용합니다. SSO에서 내려오는 role은 관리자 권한 판단에 사용하지 않습니다.
- 마스터 비밀번호 로그인: `ADMIN_MASTER_PASSWORD`와 일치하면 `adminMasterToken` HttpOnly 쿠키를 발급합니다. 기본 만료 시간은 `security.admin.master-token-ttl`이며 현재 기본값은 `1h`입니다.

SSO 로그인 후에도 관리자 접근 가능 여부는 프로젝트 DB의 `Member.role = ADMIN`으로만 판단합니다. 최초 관리자는 운영자가 DB에서 `member.role`을 `ADMIN`으로 수동 부여해야 합니다.

현재 관리자 화면에서 제공하는 기능:

- default overview shortcut 목록 조회·저장
- API 문서 공개 상태 조회·변경

default shortcut 저장은 브라우저 `fetch`로 JSON body를 전송합니다. 요청이 성공하면 기존 `default_overview_shortcut` rows를 삭제하고 요청 목록으로 다시 저장합니다. 기존 사용자 `overview_shortcut` rows는 건드리지 않습니다.

관리자 default shortcut은 최대 10개까지 등록할 수 있습니다. `sortOrder`는 `0`부터 `n - 1`까지 중복 없이 포함되어야 하며, `iconUrl`과 `targetUrl`은 `http` 또는 `https` URL만 허용합니다. `name`은 공백일 수 없고 최대 10자입니다.

## 보안 동작

현재 보안 설정은 HttpOnly 쿠키 기반 JWT stateless 인증을 전제로 합니다.

- 기본적으로 모든 요청은 인증이 필요합니다.
- overview shortcut API는 인증된 사용자만 접근할 수 있습니다.
- 관리자 화면과 관리자 JSON API는 관리자만 접근할 수 있습니다.
- 관리자 로그인, 관리자 정적 리소스, SSO 로그인/콜백, 토큰 재발급, Actuator health/info/prometheus는 인증 없이 접근 가능한 경로입니다.
- 공기질 조회 API는 optional 인증이며, SSE 스트림은 인증 없이 접근 가능합니다.
- `/scalar`, `/v3/api-docs`, `/v3/api-docs.yaml` 및 하위 경로는 Redis 기반 API 문서 토글이 `"true"`일 때만 접근할 수 있습니다.
- JWT 필터는 `accessToken` 쿠키를 기준으로 인증을 시도합니다.
- access token 인증 실패 시 `accessToken` 쿠키를 제거합니다.
- 관리자 페이지에서 access token 인증에 실패하면 JSON 401 대신 `/admin/login`으로 redirect합니다.
- 유효한 `adminMasterToken` 쿠키가 있으면 관리자 화면에 한해 `ROLE_ADMIN` 인증으로 처리합니다.
- refresh token 재발급 실패 시 `accessToken`, `refreshToken` 쿠키를 모두 제거합니다.
- refresh token 재발급은 Redis에 저장된 현재 jti와 요청 token의 jti가 일치할 때만 성공합니다.

## Actuator / Prometheus

Actuator는 health, info, prometheus 지표를 노출합니다. Prometheus는 actuator prometheus 경로를 scrape하면 됩니다.

## gRPC

현재 사용하는 RPC:

- `sensor.v2.SensorService/GetCurrentSensor`
- `airpurifier.v1.AirPurifierService/GetCurrentAirPurifier`

현재 센서 RPC 응답에는 기본 측정값 외에도 자체 알고리즘으로 계산한 다음 지표가 포함됩니다.

- `static_iaq`
- `estimated_eco2_ppm`
- `estimated_bvoc_ppm`
- `accuracy`
- `stabilization_progress_pct`
- `gas_percentage`
- `learning_complete_at_unix_ms`

## 현재 상태 요약

이 저장소는 중앙 백엔드의 공기질 수집·조회 흐름과 overview shortcut 기능을 구현 중입니다. gRPC polling, DB 저장, Redis refresh token 상태 저장, materialized view 기반 timeseries 조회, SSE 실시간 전송, 사용자 overview shortcut 조회·수정·재설정, 관리자 SSR 화면, SSO 로그인, JWT 쿠키 인증, Scalar/OpenAPI 문서와 Redis 기반 공개 토글, Whozin 공개 회원 조회 연동, Actuator 메트릭 노출은 구현되어 있습니다.
