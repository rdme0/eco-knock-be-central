package jnu.econovation.ecoknockbecentral.member.service

import jnu.econovation.ecoknockbecentral.EcoKnockBeCentralApplication
import jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort
import jnu.econovation.ecoknockbecentral.overview.repository.OverviewShortcutRepository
import jnu.econovation.ecoknockbecentral.overview.repository.OverviewLayoutRepository
import jnu.econovation.ecoknockbecentral.sso.dto.SSOMeDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import java.time.Instant

@SpringBootTest(
    classes = [EcoKnockBeCentralApplication::class],
)
@ActiveProfiles("dev")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class MemberServiceTest(
    private val memberService: MemberService,
    private val overviewShortcutRepository: OverviewShortcutRepository,
    private val overviewLayoutRepository: OverviewLayoutRepository,
    private val jdbcTemplate: JdbcTemplate,
) {
    private var defaultShortcutBackup: List<DefaultShortcutBackup> = emptyList()

    @BeforeEach
    fun setUp() {
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

        jdbcTemplate.update("delete from default_overview_shortcut")
        insertDefaultShortcut(
            iconUrl = "https://example.com/default-first-icon.png",
            targetUrl = "https://example.com/default-first",
            sortOrder = 0,
            name = "첫째",
        )
        insertDefaultShortcut(
            iconUrl = "https://example.com/default-second-icon.png",
            targetUrl = "https://example.com/default-second",
            sortOrder = 1,
            name = "둘째",
        )
    }

    @AfterEach
    fun tearDown() {
        jdbcTemplate.update(
            """
            delete from overview_shortcut
            where member_id in (
                select id from member where sso_member_id in (209903010001, 209903010002)
            )
            """.trimIndent()
        )
        jdbcTemplate.update(
            """
            delete from overview_layout
            where member_id in (
                select id from member where sso_member_id in (209903010001, 209903010002)
            )
            """.trimIndent()
        )
        jdbcTemplate.update(
            """
            delete from member
            where sso_member_id in (209903010001, 209903010002)
            """.trimIndent()
        )
        restoreDefaultShortcuts()
    }

    @Test
    @DisplayName("신규 회원 생성 후 default overview shortcut을 사용자 shortcut으로 초기화한다")
    fun initializesOverviewShortcutsAfterCreatingMember() {
        val memberInfo = memberService.getOrSaveFromSSO(newSsoMember(209903010001))

        val shortcuts = overviewShortcutRepository.findAllByMemberIdOrderBySortOrderAsc(memberInfo.id)

        assertThat(shortcuts.map { it.name }).containsExactly("첫째", "둘째")
        assertThat(shortcuts.map { it.sortOrder }).containsExactly(0, 1)
        assertThat(overviewLayoutRepository.findByMemberId(memberInfo.id)?.gridSize?.value).isEqualTo(3)
    }

    @Test
    @DisplayName("기존 회원 조회는 default overview shortcut을 다시 초기화하지 않는다")
    fun existingMemberDoesNotInitializeOverviewShortcutsAgain() {
        val memberInfo = memberService.getOrSaveFromSSO(newSsoMember(209903010002))

        memberService.getOrSaveFromSSO(newSsoMember(209903010002))

        val shortcuts = overviewShortcutRepository.findAllByMemberIdOrderBySortOrderAsc(memberInfo.id)
        assertThat(shortcuts.map { it.name }).containsExactly("첫째", "둘째")
        assertThat(overviewLayoutRepository.findAll().filter { it.member.id == memberInfo.id }).hasSize(1)
    }

    @Test
    @DisplayName("게스트 회원 생성은 기본 grid size와 사용자 바로가기를 초기화한다")
    fun guestInitializesOverview() {
        val guest = memberService.createGuest(Instant.now().plusSeconds(60))

        try {
            val shortcuts = overviewShortcutRepository.findAllByMemberIdOrderBySortOrderAsc(guest.id)

            assertThat(shortcuts.map { it.name }).containsExactly("첫째", "둘째")
            assertThat(overviewLayoutRepository.findByMemberId(guest.id)?.gridSize?.value).isEqualTo(3)
        } finally {
            jdbcTemplate.update("delete from overview_shortcut where member_id = ?", guest.id)
            jdbcTemplate.update("delete from overview_layout where member_id = ?", guest.id)
            jdbcTemplate.update("delete from member where id = ?", guest.id)
        }
    }

    @Test
    @DisplayName("만료된 게스트 회원 삭제는 overview 의존 데이터를 함께 삭제한다")
    fun deleteExpiredGuestDeletesOverviewDependencies() {
        val guest = memberService.createGuest(Instant.now().minusSeconds(1))

        memberService.deleteExpiredGuest(guest.id, Instant.now())

        assertThat(memberService.getEntity(guest.id)).isNull()
        assertThat(overviewShortcutRepository.findAllByMemberIdOrderBySortOrderAsc(guest.id)).isEmpty()
        assertThat(overviewLayoutRepository.findByMemberId(guest.id)).isNull()
    }

    private fun newSsoMember(ssoMemberId: Long): SSOMeDTO {
        return SSOMeDTO(
            ssoMemberId = ssoMemberId,
            name = "member-service-test-$ssoMemberId",
            cohort = Cohort(1),
            status = ActiveStatus.OB,
        )
    }

    private fun insertDefaultShortcut(
        iconUrl: String,
        targetUrl: String,
        sortOrder: Int,
        name: String,
    ) {
        jdbcTemplate.update(
            """
            insert into default_overview_shortcut (created_at, updated_at, icon_url, target_url, sort_order, name)
            values (now(), now(), ?, ?, ?, ?)
            """.trimIndent(),
            iconUrl,
            targetUrl,
            sortOrder,
            name,
        )
    }

    private fun restoreDefaultShortcuts() {
        jdbcTemplate.update("delete from default_overview_shortcut")
        defaultShortcutBackup.forEach {
            insertDefaultShortcut(
                iconUrl = it.iconUrl,
                targetUrl = it.targetUrl,
                sortOrder = it.sortOrder,
                name = it.name,
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
