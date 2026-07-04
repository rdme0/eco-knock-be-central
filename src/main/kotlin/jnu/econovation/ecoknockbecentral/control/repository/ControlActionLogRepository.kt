package jnu.econovation.ecoknockbecentral.control.repository

import jnu.econovation.ecoknockbecentral.control.model.entity.ControlActionLog
import org.springframework.data.jpa.repository.JpaRepository

interface ControlActionLogRepository : JpaRepository<ControlActionLog, Long> {
    fun findFirstByOrderByActedAtDesc(): ControlActionLog?
}
