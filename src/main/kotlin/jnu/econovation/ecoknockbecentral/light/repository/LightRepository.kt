package jnu.econovation.ecoknockbecentral.light.repository

import jnu.econovation.ecoknockbecentral.light.model.entity.LightReport
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LightRepository : JpaRepository<LightReport, Long>