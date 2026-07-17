package jnu.econovation.ecoknockbecentral.ai.service.internal

import jnu.econovation.ecoknockbecentral.ai.dto.aiserver.RawAIServerResponseDTO
import jnu.econovation.ecoknockbecentral.ai.dto.internal.AIChatHistoryDTO
import jnu.econovation.ecoknockbecentral.ai.model.entity.AIChatHistory
import jnu.econovation.ecoknockbecentral.ai.repository.AIChatHistoryRepository
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.service.MemberService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AIChatHistoryService(
    private val memberService: MemberService,
    private val repository: AIChatHistoryRepository
) {

    @Transactional
    fun saveChat(
        memberInfo: MemberInfoDTO,
        question: String,
        answer: String,
        rawResponse: RawAIServerResponseDTO,
    ) {
        val member = memberService.getEntityOrThrow(memberInfo.id)

        repository.save(
            AIChatHistory.builder()
                .member(member)
                .question(question)
                .answer(answer)
                .rawResponse(rawResponse)
                .build()
        )
    }

    @Transactional(readOnly = true)
    fun getChatHistory(memberInfo: MemberInfoDTO): List<AIChatHistoryDTO> {
        return repository
            .findTop20ByMemberIdOrderByCreatedAtDescIdDesc(memberInfo.id)
            .take(20)
            .asReversed()
            .map(AIChatHistoryDTO::from)
    }
}
