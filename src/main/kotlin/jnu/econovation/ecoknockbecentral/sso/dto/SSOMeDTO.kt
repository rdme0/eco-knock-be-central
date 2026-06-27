package jnu.econovation.ecoknockbecentral.sso.dto

import jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort
import jnu.econovation.ecoknockbecentral.sso.dto.response.SSOMeResponse

data class SSOMeDTO(
    val ssoMemberId: Long,
    val name: String,
    val cohort: Cohort,
    val status: ActiveStatus,
) {
    companion object {
        fun from(response: SSOMeResponse): SSOMeDTO {
            return SSOMeDTO(
                ssoMemberId = response.memberId,
                name = response.name,
                cohort = Cohort(response.generation),
                status = ActiveStatus.from(response.status)
                    ?: throw IllegalStateException("올바르지 않은 Active Status : ${response.status}"),
            )
        }
    }
}