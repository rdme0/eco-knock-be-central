package jnu.econovation.ecoknockbecentral.control.repository

import jnu.econovation.ecoknockbecentral.control.model.entity.ControlSetting
import org.springframework.data.jpa.repository.JpaRepository

interface ControlSettingRepository : JpaRepository<ControlSetting, Long>
