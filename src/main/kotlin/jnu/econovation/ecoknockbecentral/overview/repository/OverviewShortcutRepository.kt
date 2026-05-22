package jnu.econovation.ecoknockbecentral.overview.repository

import jnu.econovation.ecoknockbecentral.overview.model.entity.OverviewShortcut
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OverviewShortcutRepository : JpaRepository<OverviewShortcut, Long> {

    fun findAllByMemberIdOrderBySortOrderAsc(memberId: Long): List<OverviewShortcut>

    fun deleteAllByMemberId(memberId: Long)

}