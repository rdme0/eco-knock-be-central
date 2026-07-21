package jnu.econovation.ecoknockbecentral.overview.repository

import jnu.econovation.ecoknockbecentral.overview.model.entity.OverviewLayout
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OverviewLayoutRepository : JpaRepository<OverviewLayout, Long> {
    fun findByMemberId(memberId: Long): OverviewLayout?

    fun existsByMemberId(memberId: Long): Boolean
}
