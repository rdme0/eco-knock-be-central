package jnu.econovation.ecoknockbecentral.auth.controller

import jnu.econovation.ecoknockbecentral.EcoKnockBeCentralApplication
import jnu.econovation.ecoknockbecentral.auth.constant.AuthConstant.ACCESS_TOKEN
import jnu.econovation.ecoknockbecentral.common.security.util.JwtUtil
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.model.entity.Member
import jnu.econovation.ecoknockbecentral.member.repository.MemberRepository
import jnu.econovation.ecoknockbecentral.overview.service.OverviewService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.web.client.RestClient
import java.time.Duration
import java.time.Instant

@SpringBootTest(
    classes = [EcoKnockBeCentralApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ActiveProfiles("dev")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class GuestAuthorizationE2ETest(
    @param:LocalServerPort private val port: Int,
    private val memberRepository: MemberRepository,
    private val jwtUtil: JwtUtil,
    private val overviewService: OverviewService,
    private val jdbcTemplate: JdbcTemplate,
) {
    private val memberIds = mutableListOf<Long>()
    private val restClient: RestClient = RestClient.builder()
        .baseUrl("http://localhost:$port")
        .build()

    @AfterEach
    fun tearDown() {
        memberIds.forEach {
            jdbcTemplate.update("delete from overview_shortcut where member_id = ?", it)
            jdbcTemplate.update("delete from overview_layout where member_id = ?", it)
            jdbcTemplate.update("delete from member_wallet where member_id = ?", it)
            jdbcTemplate.update("delete from member where id = ?", it)
        }
    }

    @Test
    @DisplayName("게스트는 allowlist에 등록된 바로가기 조회만 할 수 있다")
    fun guestCanReadOnlyAllowlistedEndpoint() {
        val accessToken = createGuestAccessToken()

        val overviewStatus = requestStatus("/overview/shortcuts", accessToken)
        val aiStatus = requestStatus("/ai/chat", accessToken, org.springframework.http.HttpMethod.POST)
        val updateStatus = requestStatus("/overview/shortcuts", accessToken, org.springframework.http.HttpMethod.PUT)
        val layoutUpdateStatus = requestPutStatus("/overview/layout", accessToken, """{"gridSize":2}""")

        assertThat(overviewStatus).isEqualTo(HttpStatus.OK)
        assertThat(aiStatus).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(updateStatus).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(layoutUpdateStatus).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    @DisplayName("일반 회원과 관리자는 overview grid size를 수정할 수 있다")
    fun userAndAdminCanUpdateOverviewLayout() {
        val user = createMemberAccessToken(admin = false)
        val admin = createMemberAccessToken(admin = true)

        assertThat(requestPutStatus("/overview/layout", user, """{"gridSize":2}""")).isEqualTo(HttpStatus.OK)
        assertThat(requestPutStatus("/overview/layout", admin, """{"gridSize":2}""")).isEqualTo(HttpStatus.OK)
    }

    private fun createGuestAccessToken(): String {
        val member = memberRepository.saveAndFlush(Member.createGuest(Instant.now().plus(Duration.ofHours(1))))
        memberIds += member.id
        overviewService.initializeOverview(member.id)

        return jwtUtil.generateAccessToken(MemberInfoDTO.from(member), Duration.ofHours(1))
    }

    private fun createMemberAccessToken(admin: Boolean): String {
        val member = Member.builder()
            .ssoMemberId(System.nanoTime())
            .cohort(jnu.econovation.ecoknockbecentral.member.model.vo.Cohort(1))
            .name("overview-layout-e2e")
            .status(jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus.OB)
            .build()
        if (admin) {
            member.promoteToAdmin()
        }
        memberRepository.saveAndFlush(member)
        memberIds += member.id
        overviewService.initializeOverview(member.id)

        return jwtUtil.generateAccessToken(MemberInfoDTO.from(member), Duration.ofHours(1))
    }

    private fun requestPutStatus(path: String, accessToken: String, body: String): HttpStatus {
        return restClient.put()
            .uri(path)
            .header(HttpHeaders.COOKIE, "$ACCESS_TOKEN=$accessToken")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .body(body)
            .exchange { _, response -> HttpStatus.valueOf(response.statusCode.value()) }
    }

    private fun requestStatus(
        path: String,
        accessToken: String,
        method: org.springframework.http.HttpMethod = org.springframework.http.HttpMethod.GET,
    ): HttpStatus {
        return restClient.method(method)
            .uri(path)
            .header(HttpHeaders.COOKIE, "$ACCESS_TOKEN=$accessToken")
            .exchange { _, response -> HttpStatus.valueOf(response.statusCode.value()) }
    }
}
