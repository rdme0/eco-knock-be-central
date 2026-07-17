package jnu.econovation.ecoknockbecentral.ai.service.internal

import jnu.econovation.ecoknockbecentral.ai.model.entity.AIChatHistory
import jnu.econovation.ecoknockbecentral.ai.repository.AIChatHistoryRepository
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort
import jnu.econovation.ecoknockbecentral.member.model.vo.Role
import jnu.econovation.ecoknockbecentral.member.service.MemberService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class AIChatHistoryServiceTest {
    private val memberService = mock(MemberService::class.java)
    private val repository = mock(AIChatHistoryRepository::class.java)
    private val service = AIChatHistoryService(memberService, repository)
    private val memberInfo = MemberInfoDTO(
        1L,
        100L,
        Role.USER,
        Cohort(1),
        "테스트 회원",
        ActiveStatus.OB,
    )

    @Test
    fun `최근 이력을 DTO로 변환하고 오래된 순서로 반환한다`() {
        val histories = (20 downTo 1).map { chatHistory("질문$it", "답변$it") }
        `when`(repository.findTop20ByMemberIdOrderByCreatedAtDescIdDesc(memberInfo.id)).thenReturn(histories)

        val result = service.getChatHistory(memberInfo)

        assertThat(result).extracting<String> { it.question }
            .containsExactlyElementsOf((1..20).map { "질문$it" })
    }

    private fun chatHistory(question: String, answer: String): AIChatHistory {
        return mock(AIChatHistory::class.java).also {
            `when`(it.question).thenReturn(question)
            `when`(it.answer).thenReturn(answer)
        }
    }
}
