package jnu.econovation.ecoknockbecentral.member.initializer

import jnu.econovation.ecoknockbecentral.common.security.util.AES256Util
import jnu.econovation.ecoknockbecentral.overview.service.OverviewService
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@Component
class AdminInitializer(
    private val jdbcTemplate: JdbcTemplate,
    private val aes256Util: AES256Util,
    private val overviewService: OverviewService,
    private val transactionTemplate: TransactionTemplate,
) {
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener(ApplicationReadyEvent::class)
    fun initialize() {
        transactionTemplate.executeWithoutResult {
            deleteReservedMemberData()
            deleteReservedSsoMembers()
            upsertSystemAdmin()
        }

        overviewService.initializeOverview(SYSTEM_ADMIN_MEMBER_ID)
    }

    private fun deleteReservedMemberData() {
        val reservedMemberIds = "select id from member where id = ? or sso_member_id = ?"

        jdbcTemplate.update(
            "delete from overview_shortcut where member_id in ($reservedMemberIds)",
            SYSTEM_ADMIN_MEMBER_ID,
            SYSTEM_ADMIN_SSO_MEMBER_ID,
        )
        jdbcTemplate.update(
            "delete from overview_layout where member_id in ($reservedMemberIds)",
            SYSTEM_ADMIN_MEMBER_ID,
            SYSTEM_ADMIN_SSO_MEMBER_ID,
        )
        jdbcTemplate.update(
            "delete from ai_chat_history where member_id in ($reservedMemberIds)",
            SYSTEM_ADMIN_MEMBER_ID,
            SYSTEM_ADMIN_SSO_MEMBER_ID,
        )
        jdbcTemplate.update(
            "delete from member_wallet where member_id in ($reservedMemberIds)",
            SYSTEM_ADMIN_MEMBER_ID,
            SYSTEM_ADMIN_SSO_MEMBER_ID,
        )
    }

    private fun deleteReservedSsoMembers() {
        jdbcTemplate.update(
            "delete from member where id <> ? and sso_member_id = ?",
            SYSTEM_ADMIN_MEMBER_ID,
            SYSTEM_ADMIN_SSO_MEMBER_ID,
        )
    }

    private fun upsertSystemAdmin() {
        jdbcTemplate.update(
            """
            insert into member (id, sso_member_id, role, name)
            values (?, ?, 'ADMIN', ?)
            on conflict (id) do update
            set sso_member_id = excluded.sso_member_id,
                role = excluded.role,
                name = excluded.name
            """.trimIndent(),
            SYSTEM_ADMIN_MEMBER_ID,
            SYSTEM_ADMIN_SSO_MEMBER_ID,
            aes256Util.encrypt(SYSTEM_ADMIN_NAME),
        )
    }

    private companion object {
        const val SYSTEM_ADMIN_MEMBER_ID = 0L
        const val SYSTEM_ADMIN_SSO_MEMBER_ID = 0L
        const val SYSTEM_ADMIN_NAME = "관리자"
    }
}
