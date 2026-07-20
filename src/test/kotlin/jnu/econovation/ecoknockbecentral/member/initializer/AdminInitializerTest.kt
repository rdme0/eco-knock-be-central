package jnu.econovation.ecoknockbecentral.member.initializer

import jnu.econovation.ecoknockbecentral.EcoKnockBeCentralApplication
import jnu.econovation.ecoknockbecentral.common.security.util.AES256Util
import jnu.econovation.ecoknockbecentral.member.model.entity.Member
import jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort
import jnu.econovation.ecoknockbecentral.member.model.vo.Role
import jnu.econovation.ecoknockbecentral.member.repository.MemberRepository
import jnu.econovation.ecoknockbecentral.wallet.service.MemberWalletService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor

@SpringBootTest(classes = [EcoKnockBeCentralApplication::class])
@ActiveProfiles("dev")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class AdminInitializerTest(
    private val adminInitializer: AdminInitializer,
    private val memberWalletService: MemberWalletService,
    private val memberRepository: MemberRepository,
    private val jdbcTemplate: JdbcTemplate,
    private val aes256Util: AES256Util,
) {
    @BeforeEach
    fun resetSystemAdmin() {
        adminInitializer.initialize()
        memberWalletService.createManagedWalletIfAbsent(SYSTEM_ADMIN_MEMBER_ID)
    }

    @Test
    @DisplayName("시스템 관리자는 ID와 SSO ID가 모두 0인 ADMIN 회원으로 암호화해 생성한다")
    fun initializesSystemAdmin() {
        val admin = memberRepository.findById(SYSTEM_ADMIN_MEMBER_ID).orElseThrow()
        val encryptedName = jdbcTemplate.queryForObject(
            "select name from member where id = ?",
            String::class.java,
            SYSTEM_ADMIN_MEMBER_ID,
        )

        assertThat(admin.id).isEqualTo(SYSTEM_ADMIN_MEMBER_ID)
        assertThat(admin.ssoMemberId).isEqualTo(SYSTEM_ADMIN_SSO_MEMBER_ID)
        assertThat(admin.role).isEqualTo(Role.ADMIN)
        assertThat(admin.name).isEqualTo(SYSTEM_ADMIN_NAME)
        assertThat(encryptedName).isNotEqualTo(SYSTEM_ADMIN_NAME)
        assertThat(aes256Util.decrypt(encryptedName)).isEqualTo(SYSTEM_ADMIN_NAME)
    }

    @Test
    @DisplayName("예약 ID 또는 SSO ID 회원의 데이터를 정리하고 시스템 관리자를 재생성한다")
    fun resetsReservedMembersAndTheirData() {
        removeSystemAdmin()
        insertMember(SYSTEM_ADMIN_MEMBER_ID, 1L, Role.USER)
        val reservedSsoMember = memberRepository.save(
            Member.builder()
                .ssoMemberId(SYSTEM_ADMIN_SSO_MEMBER_ID)
                .cohort(Cohort(1))
                .name("reserved-sso-member")
                .status(ActiveStatus.OB)
                .build(),
        )
        insertMemberData(SYSTEM_ADMIN_MEMBER_ID, "0")
        insertMemberData(reservedSsoMember.id, "1")

        adminInitializer.initialize()

        val admin = memberRepository.findById(SYSTEM_ADMIN_MEMBER_ID).orElseThrow()
        val overviewCount = jdbcTemplate.queryForObject(
            "select count(*) from overview_shortcut where member_id = ?",
            Int::class.java,
            SYSTEM_ADMIN_MEMBER_ID,
        )
        val defaultOverviewCount = jdbcTemplate.queryForObject(
            "select count(*) from default_overview_shortcut",
            Int::class.java,
        )

        assertThat(admin.ssoMemberId).isEqualTo(SYSTEM_ADMIN_SSO_MEMBER_ID)
        assertThat(admin.role).isEqualTo(Role.ADMIN)
        assertThat(memberRepository.findById(reservedSsoMember.id)).isEmpty
        assertThat(jdbcTemplate.queryForObject("select count(*) from ai_chat_history", Int::class.java)).isZero
        assertThat(jdbcTemplate.queryForObject("select count(*) from member_wallet", Int::class.java)).isZero
        assertThat(overviewCount).isEqualTo(defaultOverviewCount)

        memberWalletService.createManagedWalletIfAbsent(SYSTEM_ADMIN_MEMBER_ID)
    }

    private fun removeSystemAdmin() {
        jdbcTemplate.update("delete from overview_shortcut where member_id = ?", SYSTEM_ADMIN_MEMBER_ID)
        jdbcTemplate.update("delete from ai_chat_history where member_id = ?", SYSTEM_ADMIN_MEMBER_ID)
        jdbcTemplate.update("delete from member_wallet where member_id = ?", SYSTEM_ADMIN_MEMBER_ID)
        jdbcTemplate.update("delete from member where id = ?", SYSTEM_ADMIN_MEMBER_ID)
    }

    private fun insertMember(id: Long, ssoMemberId: Long, role: Role) {
        jdbcTemplate.update(
            "insert into member (id, sso_member_id, role, name) values (?, ?, ?, ?)",
            id,
            ssoMemberId,
            role.name,
            aes256Util.encrypt("reserved-member-$id"),
        )
    }

    private fun insertMemberData(memberId: Long, suffix: String) {
        jdbcTemplate.update(
            """
            insert into overview_shortcut (member_id, icon_url, target_url, sort_order, name)
            values (?, 'https://example.com/icon.png', 'https://example.com', 0, 'reserved')
            """.trimIndent(),
            memberId,
        )
        jdbcTemplate.update(
            """
            insert into ai_chat_history (member_id, question, answer, raw_response)
            values (?, 'question', 'answer', '{}'::jsonb)
            """.trimIndent(),
            memberId,
        )
        jdbcTemplate.update(
            """
            insert into member_wallet (member_id, wallet_address, encrypted_private_key, wallet_type, is_active)
            values (?, ?, 'private-key', 'MANAGED', true)
            """.trimIndent(),
            memberId,
            "0x${suffix.padStart(40, '0')}",
        )
    }

    private companion object {
        const val SYSTEM_ADMIN_MEMBER_ID = 0L
        const val SYSTEM_ADMIN_SSO_MEMBER_ID = 0L
        const val SYSTEM_ADMIN_NAME = "관리자"
    }
}
