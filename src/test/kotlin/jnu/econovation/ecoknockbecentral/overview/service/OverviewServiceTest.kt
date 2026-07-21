package jnu.econovation.ecoknockbecentral.overview.service

import jnu.econovation.ecoknockbecentral.EcoKnockBeCentralApplication
import jnu.econovation.ecoknockbecentral.member.model.entity.Member
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort
import jnu.econovation.ecoknockbecentral.member.repository.MemberRepository
import jnu.econovation.ecoknockbecentral.overview.dto.request.ReplaceDefaultOverviewShortcutsRequest
import jnu.econovation.ecoknockbecentral.overview.dto.request.UpdateOverviewLayoutRequest
import jnu.econovation.ecoknockbecentral.overview.dto.request.UpdateDefaultShortcutDTO
import jnu.econovation.ecoknockbecentral.overview.model.entity.OverviewShortcut
import jnu.econovation.ecoknockbecentral.overview.model.entity.OverviewLayout
import jnu.econovation.ecoknockbecentral.overview.model.vo.ValidHttpUrl
import jnu.econovation.ecoknockbecentral.overview.model.vo.GridSize
import jnu.econovation.ecoknockbecentral.overview.exception.OverviewLayoutGridSizeConflictException
import jnu.econovation.ecoknockbecentral.overview.repository.OverviewLayoutRepository
import jnu.econovation.ecoknockbecentral.overview.repository.OverviewShortcutRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
    private val overviewLayoutRepository: OverviewLayoutRepository,
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
                        iconUrl = null,
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
        assertThat(defaults.first().iconUrl).isNull()
        assertThat(userShortcuts).hasSize(1)
        assertThat(userShortcuts.first().name).isEqualTo("내목록")
    }

    @Test
    @DisplayName("overview shortcut 조회는 grid size와 바로가기 목록을 함께 반환한다")
    fun getOverviewShortcutsIncludesGridSize() {
        val member = newMember(209901010003)
        createOverviewLayout(member)

        val response = overviewService.getOverviewShortcuts(MemberInfoDTO.from(member))

        assertThat(response.gridSize).isEqualTo(3)
        assertThat(response.shortcuts).isNotNull()
    }

    @Test
    @DisplayName("overview grid size는 2 또는 3으로 변경할 수 있고 동일한 값이면 충돌이다")
    fun updateOverviewLayout() {
        val member = newMember(209901010004)
        createOverviewLayout(member)

        overviewService.updateOverviewLayout(MemberInfoDTO.from(member), UpdateOverviewLayoutRequest(GridSize(2)))

        assertThat(overviewLayoutRepository.findByMemberId(member.id)?.gridSize?.value).isEqualTo(2)
        assertThatThrownBy {
            overviewService.updateOverviewLayout(MemberInfoDTO.from(member), UpdateOverviewLayoutRequest(GridSize(2)))
        }.isInstanceOf(OverviewLayoutGridSizeConflictException::class.java)
    }

    @Test
    @DisplayName("지원하지 않는 overview grid size는 요청 문법 오류다")
    fun invalidGridSize() {
        assertThatThrownBy { GridSize(4) }
            .isInstanceOf(jnu.econovation.ecoknockbecentral.common.exception.client.BadDataSyntaxException::class.java)
    }

    private fun newMember(ssoMemberId: Long): Member {
        return memberRepository.save(
            Member.builder()
                .ssoMemberId(ssoMemberId)
                .cohort(Cohort(1))
                .name("overview-layout-test-$ssoMemberId")
                .status(ActiveStatus.OB)
                .build()
        )
    }

    private fun createOverviewLayout(member: Member) {
        overviewLayoutRepository.save(
            OverviewLayout.builder()
                .member(member)
                .gridSize(GridSize(3))
                .build()
        )
    }
}
