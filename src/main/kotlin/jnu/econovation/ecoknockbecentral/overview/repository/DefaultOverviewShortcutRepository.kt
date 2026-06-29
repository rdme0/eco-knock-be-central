package jnu.econovation.ecoknockbecentral.overview.repository

import jnu.econovation.ecoknockbecentral.overview.model.entity.DefaultOverviewShortcut
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DefaultOverviewShortcutRepository : JpaRepository<DefaultOverviewShortcut, Long> {

    fun findAllByOrderBySortOrderAsc(): List<DefaultOverviewShortcut>

}
