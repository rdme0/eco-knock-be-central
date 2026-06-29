package jnu.econovation.ecoknockbecentral.overview.service

import jnu.econovation.ecoknockbecentral.EcoKnockBeCentralApplication
import jnu.econovation.ecoknockbecentral.member.model.entity.Member
import jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort
import jnu.econovation.ecoknockbecentral.member.repository.MemberRepository
import jnu.econovation.ecoknockbecentral.overview.dto.request.ReplaceDefaultOverviewShortcutsRequest
import jnu.econovation.ecoknockbecentral.overview.dto.request.UpdateDefaultShortcutDTO
import jnu.econovation.ecoknockbecentral.overview.model.entity.OverviewShortcut
import jnu.econovation.ecoknockbecentral.overview.model.vo.ValidHttpUrl
import jnu.econovation.ecoknockbecentral.overview.repository.OverviewShortcutRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(
    classes = [EcoKnockBeCentralApplication::class],
)
@ActiveProfiles("dev")
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class OverviewServiceTest(
    private val overviewService: OverviewService,
    private val memberRepository: MemberRepository,
    private val overviewShortcutRepository: OverviewShortcutRepository,
) {
    @Test
    @DisplayName("default overview shortcut 전체 교체는 정렬 조회를 보장하고 사용자 shortcut을 건드리지 않는다")
    fun replaceDefaultOverviewShortcutsKeepsUserShortcutsIndependent() {
        val member = memberRepository.save(
            Member.builder()
                .ssoMemberId(209901010001)
                .cohort(Cohort(1))
                .name("overview-service-test")
                .status(ActiveStatus.OB)
                .build()
        )
        overviewShortcutRepository.save(
            OverviewShortcut.builder()
                .member(member)
                .name("내목록")
                .iconUrl(ValidHttpUrl("https://example.com/my-icon.png"))
                .targetUrl(ValidHttpUrl("https://example.com/my"))
                .sortOrder(0)
                .build()
        )

        overviewService.replaceDefaultOverviewShortcuts(
            ReplaceDefaultOverviewShortcutsRequest(
                shortcuts = mutableListOf(
                    UpdateDefaultShortcutDTO(
                        iconUrl = ValidHttpUrl("https://example.com/second-icon.png"),
                        targetUrl = ValidHttpUrl("https://example.com/second"),
                        sortOrder = 1,
                        name = "둘째",
                    ),
                    UpdateDefaultShortcutDTO(
                        iconUrl = ValidHttpUrl("https://example.com/first-icon.png"),
                        targetUrl = ValidHttpUrl("https://example.com/first"),
                        sortOrder = 0,
                        name = "첫째",
                    ),
                )
            )
        )

        val defaults = overviewService.getDefaultOverviewShortcuts()
        val userShortcuts = overviewShortcutRepository.findAllByMemberIdOrderBySortOrderAsc(member.id)

        assertThat(defaults.map { it.name }).containsExactly("첫째", "둘째")
        assertThat(defaults.map { it.sortOrder }).containsExactly(0, 1)
        assertThat(userShortcuts).hasSize(1)
        assertThat(userShortcuts.first().name).isEqualTo("내목록")
    }
}
