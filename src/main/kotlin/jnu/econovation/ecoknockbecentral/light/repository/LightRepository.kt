package jnu.econovation.ecoknockbecentral.light.repository

import jnu.econovation.ecoknockbecentral.light.dto.LightReportResultDTO
import jnu.econovation.ecoknockbecentral.light.model.entity.LightReport
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface LightRepository : JpaRepository<LightReport, Long> {
    @Query(
        """
        select new jnu.econovation.ecoknockbecentral.light.dto.LightReportResultDTO(
            count(l),
            min(l.lux),
            avg(l.lux),
            max(l.lux),
            max(l.measuredAt)
        )
        from LightReport l
        where l.measuredAt >= :from
          and l.measuredAt < :to
        """
    )
    fun findReport(
        from: Instant,
        to: Instant,
    ): LightReportResultDTO
}
