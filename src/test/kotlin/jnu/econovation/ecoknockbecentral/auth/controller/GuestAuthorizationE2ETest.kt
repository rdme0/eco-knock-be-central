package jnu.econovation.ecoknockbecentral.auth.controller

import jnu.econovation.ecoknockbecentral.EcoKnockBeCentralApplication
import jnu.econovation.ecoknockbecentral.auth.constant.AuthConstant.ACCESS_TOKEN
import jnu.econovation.ecoknockbecentral.common.security.util.JwtUtil
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.model.entity.Member
import jnu.econovation.ecoknockbecentral.member.repository.MemberRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
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
    @LocalServerPort private val port: Int,
    private val memberRepository: MemberRepository,
    private val jwtUtil: JwtUtil,
) {
    private val memberIds = mutableListOf<Long>()
    private val restClient: RestClient = RestClient.builder()
        .baseUrl("http://localhost:$port")
        .build()

    @AfterEach
    fun tearDown() {
        memberIds.forEach(memberRepository::deleteById)
    }

    @Test
    @DisplayName("게스트는 allowlist에 등록된 바로가기 조회만 할 수 있다")
    fun guestCanReadOnlyAllowlistedEndpoint() {
        val accessToken = createGuestAccessToken()

        val overviewStatus = requestStatus("/overview/shortcuts", accessToken)
        val aiStatus = requestStatus("/ai/chat", accessToken, org.springframework.http.HttpMethod.POST)
        val updateStatus = requestStatus("/overview/shortcuts", accessToken, org.springframework.http.HttpMethod.PUT)

        assertThat(overviewStatus).isEqualTo(HttpStatus.OK)
        assertThat(aiStatus).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(updateStatus).isEqualTo(HttpStatus.FORBIDDEN)
    }

    private fun createGuestAccessToken(): String {
        val member = memberRepository.saveAndFlush(Member.createGuest(Instant.now().plus(Duration.ofHours(1))))
        memberIds += member.id

        return jwtUtil.generateAccessToken(MemberInfoDTO.from(member), Duration.ofHours(1))
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
