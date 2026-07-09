package jnu.econovation.ecoknockbecentral.overview.controller

import jnu.econovation.ecoknockbecentral.EcoKnockBeCentralApplication
import jnu.econovation.ecoknockbecentral.auth.constant.AuthConstant.ACCESS_TOKEN
import jnu.econovation.ecoknockbecentral.auth.constant.AuthConstant.REFRESH_TOKEN
import jnu.econovation.ecoknockbecentral.common.security.util.JwtUtil
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.model.entity.Member
import jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort
import jnu.econovation.ecoknockbecentral.member.repository.MemberRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.web.client.RestClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@SpringBootTest(
    classes = [EcoKnockBeCentralApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["security.test-auth.sso-member-id=209902010001"],
)
@ActiveProfiles("dev")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class AdminOverviewShortcutControllerE2ETest(
    @param:LocalServerPort
    private val port: Int,
    private val memberRepository: MemberRepository,
    private val jwtUtil: JwtUtil,
    private val jdbcTemplate: JdbcTemplate,
) {
    private val restClient: RestClient = RestClient.builder()
        .baseUrl("http://localhost:$port")
        .build()

    private var defaultShortcutBackup: List<DefaultShortcutBackup> = emptyList()

    @BeforeEach
    fun backUpDefaultShortcuts() {
        defaultShortcutBackup = jdbcTemplate.query(
            """
            select icon_url, target_url, sort_order, name
            from default_overview_shortcut
            order by sort_order
            """.trimIndent()
        ) { resultSet, _ ->
            DefaultShortcutBackup(
                iconUrl = resultSet.getString("icon_url"),
                targetUrl = resultSet.getString("target_url"),
                sortOrder = resultSet.getInt("sort_order"),
                name = resultSet.getString("name"),
            )
        }
    }

    @AfterEach
    fun tearDown() {
        jdbcTemplate.update(
            """
            delete from member
            where sso_member_id in (209902010001, 209902010002)
            """.trimIndent()
        )
        restoreDefaultShortcuts()
    }

    @Test
    @DisplayName("미인증 관리자는 관리자 로그인 화면으로 이동한다")
    fun unauthenticatedAdminPageRedirectsToLogin() {
        val response = request(HttpMethod.GET, "/admin")

        assertThat(response.statusCode).isEqualTo(HttpStatus.FOUND)
        assertThat(response.headers.location.toString()).endsWith("/admin/login")
    }

    @Test
    @DisplayName("마스터 비밀번호가 맞으면 관리자 전용 쿠키를 발급하고 관리자 화면으로 이동한다")
    fun masterPasswordLoginIssuesAdminCookie() {
        val response = postForm(
            path = "/admin/login/master",
            body = adminMasterPasswordFormBody(),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FOUND)
        assertThat(response.headers.location.toString()).endsWith("/admin")
        assertThat(response.headers[HttpHeaders.SET_COOKIE])
            .anyMatch { it.startsWith("adminMasterToken=") }
    }

    @Test
    @DisplayName("관리자 로그인 화면의 SSO 링크는 관리자 홈으로 이동하도록 생성된다")
    fun adminLoginPageUsesAdminHomeRedirect() {
        val response = request(HttpMethod.GET, "/admin/login")

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("/sso/login")
        assertThat(response.body).contains("redirect=http://localhost:$port/admin")
        assertThat(response.body).doesNotContain("redirect=http://localhost:$port/admin/overview-shortcuts")
        assertThat(response.body).contains("/admin/login.css")
    }

    @Test
    @DisplayName("마스터 비밀번호가 틀리면 로그인 화면에 에러를 표시한다")
    fun wrongMasterPasswordReturnsLoginPageWithError() {
        val response = postForm(
            path = "/admin/login/master",
            body = "password=wrong-password",
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.headers[HttpHeaders.SET_COOKIE].orEmpty())
            .noneMatch { it.startsWith("adminMasterToken=") }
        assertThat(response.body).contains("마스터 비밀번호가 올바르지 않습니다.")
    }

    @Test
    @DisplayName("마스터 비밀번호가 맞으면 테스트 계정의 일반 인증 쿠키를 발급한다")
    fun masterPasswordIssuesTestAuthCookies() {
        saveMember(209902010001, promoteToAdmin = false)

        val response = postJson(
            path = "/auth/test-token",
            body = adminMasterPasswordJsonBody(),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("\"isSuccess\":true")
        assertThat(response.headers[HttpHeaders.SET_COOKIE])
            .anyMatch { it.startsWith("$ACCESS_TOKEN=") }
        assertThat(response.headers[HttpHeaders.SET_COOKIE])
            .anyMatch { it.startsWith("$REFRESH_TOKEN=") }
    }

    @Test
    @DisplayName("테스트 인증 쿠키 발급은 마스터 비밀번호가 틀리면 실패한다")
    fun wrongMasterPasswordDoesNotIssueTestAuthCookies() {
        saveMember(209902010001, promoteToAdmin = false)

        val response = postJson(
            path = "/auth/test-token",
            body = """{"password":"wrong-password"}""",
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(response.headers[HttpHeaders.SET_COOKIE].orEmpty())
            .noneMatch { it.startsWith("$ACCESS_TOKEN=") || it.startsWith("$REFRESH_TOKEN=") }
    }

    @Test
    @DisplayName("마스터 관리자 쿠키만으로 default overview shortcut 관리자 화면에 접근할 수 있다")
    fun masterCookieCanOpenOverviewShortcutPage() {
        val response = request(
            method = HttpMethod.GET,
            path = "/admin/overview-shortcuts",
            adminMasterToken = jwtUtil.generateAdminMasterToken(java.time.Duration.ofHours(1)),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.headers.contentType.toString()).startsWith(MediaType.TEXT_HTML_VALUE)
        assertThat(response.body).contains("기본 모아두기")
        assertThat(response.body).contains("previewRoot")
        assertThat(response.body).contains("previewShortcutGrid")
        assertThat(response.body).contains("/admin/overview-shortcuts.css")
        assertThat(response.body).contains("/admin/overview-shortcuts.js")
    }

    @Test
    @DisplayName("마스터 관리자 쿠키만으로 관리자 홈에 접근할 수 있다")
    fun masterCookieCanOpenAdminHome() {
        val response = request(
            method = HttpMethod.GET,
            path = "/admin/",
            adminMasterToken = jwtUtil.generateAdminMasterToken(java.time.Duration.ofHours(1)),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.headers.contentType.toString()).startsWith(MediaType.TEXT_HTML_VALUE)
        assertThat(response.body).contains("관리자 기능")
        assertThat(response.body).contains("기본 모아두기 설정하기")
        assertThat(response.body).contains("/admin/overview-shortcuts")
    }

    @Test
    @DisplayName("관리자 화면 정적 CSS와 JS는 인증 없이 로드된다")
    fun adminStaticResourcesArePublic() {
        val cssResponse = request(HttpMethod.GET, "/admin/overview-shortcuts.css")
        val jsResponse = request(HttpMethod.GET, "/admin/overview-shortcuts.js")
        val adminCssResponse = request(HttpMethod.GET, "/admin/admin.css")
        val loginCssResponse = request(HttpMethod.GET, "/admin/login.css")
        val accessDeniedCssResponse = request(HttpMethod.GET, "/admin/access-denied.css")

        assertThat(cssResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(cssResponse.body).contains("color-scheme: light")
        assertThat(cssResponse.body).contains("phone-preview")
        assertThat(jsResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(jsResponse.body).contains("fetch(form.action")
        assertThat(jsResponse.body).contains("renderPreview")
        assertThat(adminCssResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(adminCssResponse.body).contains("admin-home")
        assertThat(loginCssResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(loginCssResponse.body).contains("primary-hover")
        assertThat(accessDeniedCssResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(accessDeniedCssResponse.body).contains("place-items: center")
    }

    @Test
    @DisplayName("프로젝트 ADMIN은 default overview shortcut 관리자 화면에 접근할 수 있다")
    fun adminCanOpenOverviewShortcutPage() {
        val admin = saveMember(209902010001, promoteToAdmin = true)
        val response = request(
            method = HttpMethod.GET,
            path = "/admin/overview-shortcuts",
            accessToken = jwtUtil.generateAccessToken(MemberInfoDTO.from(admin)),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.headers.contentType.toString()).startsWith(MediaType.TEXT_HTML_VALUE)
        assertThat(response.body).contains("기본 모아두기")
    }

    @Test
    @DisplayName("프로젝트 ADMIN은 관리자 홈에 접근할 수 있다")
    fun adminCanOpenAdminHome() {
        val admin = saveMember(209902010001, promoteToAdmin = true)
        val response = request(
            method = HttpMethod.GET,
            path = "/admin/",
            accessToken = jwtUtil.generateAccessToken(MemberInfoDTO.from(admin)),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.headers.contentType.toString()).startsWith(MediaType.TEXT_HTML_VALUE)
        assertThat(response.body).contains("관리자 기능")
        assertThat(response.body).contains("기본 모아두기 설정하기")
        assertThat(response.body).contains("/admin/overview-shortcuts")
    }

    @Test
    @DisplayName("프로젝트 USER는 default overview shortcut 관리자 화면에 접근할 수 없다")
    fun userCannotOpenOverviewShortcutPage() {
        val user = saveMember(209902010002, promoteToAdmin = false)
        val response = request(
            method = HttpMethod.GET,
            path = "/admin/overview-shortcuts",
            accessToken = jwtUtil.generateAccessToken(MemberInfoDTO.from(user)),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(response.body).contains("접근 권한이 없습니다")
        assertThat(response.body).contains("/admin/access-denied.css")
    }

    @Test
    @DisplayName("프로젝트 USER는 관리자 홈에 접근할 수 없다")
    fun userCannotOpenAdminHome() {
        val user = saveMember(209902010002, promoteToAdmin = false)
        val response = request(
            method = HttpMethod.GET,
            path = "/admin",
            accessToken = jwtUtil.generateAccessToken(MemberInfoDTO.from(user)),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(response.body).contains("접근 권한이 없습니다")
        assertThat(response.body).contains("/admin/access-denied.css")
    }

    @Test
    @DisplayName("관리자는 JSON body로 default overview shortcut을 저장할 수 있다")
    fun adminCanSaveDefaultShortcutsWithJsonBody() {
        val admin = saveMember(209902010001, promoteToAdmin = true)
        val response = postJson(
            path = "/admin/overview-shortcuts",
            accessToken = jwtUtil.generateAccessToken(MemberInfoDTO.from(admin)),
            body = """
                {
                  "shortcuts": [
                    {
                      "sortOrder": 0,
                      "name": "첫째",
                      "iconUrl": "https://example.com/first-icon.png",
                      "targetUrl": "https://example.com/first"
                    },
                    {
                      "sortOrder": 1,
                      "name": "둘째",
                      "iconUrl": "https://example.com/second-icon.png",
                      "targetUrl": "https://example.com/second"
                    }
                  ]
                }
            """.trimIndent(),
        )

        val names = jdbcTemplate.queryForList(
            """
            select name
            from default_overview_shortcut
            order by sort_order
            """.trimIndent(),
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("\"isSuccess\":true")
        assertThat(names).containsExactly("첫째", "둘째")
    }

    @Test
    @DisplayName("관리자 저장 요청의 URL이 올바르지 않으면 JSON 에러를 반환한다")
    fun invalidAdminSaveReturnsJsonError() {
        val admin = saveMember(209902010001, promoteToAdmin = true)
        val response = postJson(
            path = "/admin/overview-shortcuts",
            accessToken = jwtUtil.generateAccessToken(MemberInfoDTO.from(admin)),
            body = """
                {
                  "shortcuts": [
                    {
                      "sortOrder": 0,
                      "name": "테스트",
                      "iconUrl": "ftp://example.com/icon.png",
                      "targetUrl": "https://example.com"
                    }
                  ]
                }
            """.trimIndent(),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body).contains("올바르지 않은 url 입니다.")
        assertThat(response.body).contains("\"isSuccess\":false")
    }

    @Test
    @DisplayName("관리자 로그아웃은 마스터 관리자 쿠키를 제거하고 로그인 화면으로 이동한다")
    fun logoutRemovesMasterCookie() {
        val response = restClient.post()
            .uri("/admin/logout")
            .header(
                HttpHeaders.COOKIE,
                "adminMasterToken=${jwtUtil.generateAdminMasterToken(java.time.Duration.ofHours(1))}"
            )
            .exchange { _, response ->
                org.springframework.http.ResponseEntity
                    .status(response.statusCode)
                    .headers(response.headers)
                    .body(String(response.body.readAllBytes(), StandardCharsets.UTF_8))
            }

        assertThat(response.statusCode).isEqualTo(HttpStatus.FOUND)
        assertThat(response.headers.location.toString()).endsWith("/admin/login")
        assertThat(response.headers[HttpHeaders.SET_COOKIE])
            .anyMatch { it.startsWith("adminMasterToken=;") }
    }

    private fun saveMember(
        ssoMemberId: Long,
        promoteToAdmin: Boolean,
    ): Member {
        val member = Member.builder()
            .ssoMemberId(ssoMemberId)
            .cohort(Cohort(1))
            .name("admin-overview-test-$ssoMemberId")
            .status(ActiveStatus.OB)
            .build()

        if (promoteToAdmin) {
            member.promoteToAdmin()
        }

        return memberRepository.save(member)
    }

    private fun request(
        method: HttpMethod,
        path: String,
        accessToken: String? = null,
        adminMasterToken: String? = null,
    ): ResponseEntity<String> {
        return restClient.method(method)
            .uri(path)
            .headers {
                val cookies = listOfNotNull(
                    accessToken?.let { token -> "$ACCESS_TOKEN=$token" },
                    adminMasterToken?.let { token -> "adminMasterToken=$token" },
                )
                if (cookies.isNotEmpty()) {
                    it.add(HttpHeaders.COOKIE, cookies.joinToString("; "))
                }
            }
            .exchange { _, response ->
                org.springframework.http.ResponseEntity
                    .status(response.statusCode)
                    .headers(response.headers)
                    .body(String(response.body.readAllBytes(), StandardCharsets.UTF_8))
            }
    }

    private fun postForm(
        path: String,
        body: String,
    ): ResponseEntity<String> {
        return restClient.post()
            .uri(path)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(body)
            .exchange { _, response ->
                org.springframework.http.ResponseEntity
                    .status(response.statusCode)
                    .headers(response.headers)
                    .body(String(response.body.readAllBytes(), StandardCharsets.UTF_8))
            }
    }

    private fun adminMasterPasswordFormBody(): String {
        val password = requireNotNull(System.getenv("ADMIN_MASTER_PASSWORD")) {
            "ADMIN_MASTER_PASSWORD is required for admin master password login E2E test"
        }
        return "password=${URLEncoder.encode(password, StandardCharsets.UTF_8)}"
    }

    private fun adminMasterPasswordJsonBody(): String {
        val password = requireNotNull(System.getenv("ADMIN_MASTER_PASSWORD")) {
            "ADMIN_MASTER_PASSWORD is required for test token E2E test"
        }
        return """{"password":"${escapeJson(password)}"}"""
    }

    private fun escapeJson(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
    }

    private fun postJson(
        path: String,
        body: String,
    ): ResponseEntity<String> {
        return restClient.post()
            .uri(path)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange { _, response ->
                org.springframework.http.ResponseEntity
                    .status(response.statusCode)
                    .headers(response.headers)
                    .body(String(response.body.readAllBytes(), StandardCharsets.UTF_8))
            }
    }

    private fun postJson(
        path: String,
        accessToken: String,
        body: String,
    ): ResponseEntity<String> {
        return restClient.post()
            .uri(path)
            .header(HttpHeaders.COOKIE, "$ACCESS_TOKEN=$accessToken")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange { _, response ->
                org.springframework.http.ResponseEntity
                    .status(response.statusCode)
                    .headers(response.headers)
                    .body(String(response.body.readAllBytes(), StandardCharsets.UTF_8))
            }
    }

    private fun restoreDefaultShortcuts() {
        jdbcTemplate.update("delete from default_overview_shortcut")
        defaultShortcutBackup.forEach {
            jdbcTemplate.update(
                """
                insert into default_overview_shortcut (created_at, updated_at, icon_url, target_url, sort_order, name)
                values (now(), now(), ?, ?, ?, ?)
                """.trimIndent(),
                it.iconUrl,
                it.targetUrl,
                it.sortOrder,
                it.name,
            )
        }
    }

    private data class DefaultShortcutBackup(
        val iconUrl: String,
        val targetUrl: String,
        val sortOrder: Int,
        val name: String,
    )
}
