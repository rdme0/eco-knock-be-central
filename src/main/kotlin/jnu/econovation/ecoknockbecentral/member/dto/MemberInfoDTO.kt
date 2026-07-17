package jnu.econovation.ecoknockbecentral.member.dto

import jnu.econovation.ecoknockbecentral.member.model.entity.Member
import jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort
import jnu.econovation.ecoknockbecentral.member.model.vo.Role
import java.time.Instant

data class MemberInfoDTO(
    val id: Long,
    val ssoMemberId: Long?,
    val role: Role,
    val cohort: Cohort?,
    val name: String,
    val status: ActiveStatus?,
    val guestExpiresAt: Instant? = null,
) {
    companion object {
        fun from(member: Member): MemberInfoDTO {
            return MemberInfoDTO(
                id = member.id,
                ssoMemberId = member.ssoMemberId,
                role = member.role,
                cohort = member.cohort,
                name = member.name,
                status = member.status,
                guestExpiresAt = member.guestExpiresAt,
            )
        }
    }
}
