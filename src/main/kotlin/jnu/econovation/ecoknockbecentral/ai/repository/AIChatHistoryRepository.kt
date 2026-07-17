package jnu.econovation.ecoknockbecentral.ai.repository

import jnu.econovation.ecoknockbecentral.ai.model.entity.AIChatHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AIChatHistoryRepository : JpaRepository<AIChatHistory, Long> {
    fun findTop20ByMemberIdOrderByCreatedAtDescIdDesc(memberId: Long): List<AIChatHistory>
}
