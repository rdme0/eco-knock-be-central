package jnu.econovation.ecoknockbecentral.member.initializer

import jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort
import jnu.econovation.ecoknockbecentral.member.service.MemberService
import jnu.econovation.ecoknockbecentral.sso.dto.SSOMeDTO
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
class TestAuthMemberInitializer(
    private val memberService: MemberService,
) {
    companion object {
        private const val TEST_AUTH_SSO_MEMBER_ID = 0L
        private const val TEST_AUTH_MEMBER_NAME = "테스트 회원"
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener(ApplicationReadyEvent::class)
    fun initialize() {
        memberService.getOrSaveFromSSO(
            SSOMeDTO(
                ssoMemberId = TEST_AUTH_SSO_MEMBER_ID,
                name = TEST_AUTH_MEMBER_NAME,
                cohort = Cohort(1),
                status = ActiveStatus.OB,
            )
        )
    }
}
