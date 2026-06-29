package jnu.econovation.ecoknockbecentral.overview.event

import jnu.econovation.ecoknockbecentral.member.event.MemberCreatedEvent
import jnu.econovation.ecoknockbecentral.overview.service.OverviewService
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class MemberCreatedEventListener(
    private val overviewService: OverviewService,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun initOverviewShortcuts(event: MemberCreatedEvent) {
        try {
            overviewService.initOverviewShortcuts(event.memberId)
        } catch (e: Exception) {
            logger.error(e) { "신규 회원 default overview shortcut 초기화 실패: memberId=${event.memberId}" }
        }
    }
}
