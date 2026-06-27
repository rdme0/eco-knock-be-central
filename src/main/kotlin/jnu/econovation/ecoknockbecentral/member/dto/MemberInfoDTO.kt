package jnu.econovation.ecoknockbecentral.member.dto

import jnu.econovation.ecoknockbecentral.member.model.entity.Member
import jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort
import jnu.econovation.ecoknockbecentral.member.model.vo.Role

data class MemberInfoDTO(
    val id: Long,
    val ssoMemberId: Long,
    val role: Role,
    val cohort: Cohort,
    val name: String,
    val status: ActiveStatus
) {
    companion object {
        fun from(member: Member): MemberInfoDTO {
            return MemberInfoDTO(
                id = member.id,
                ssoMemberId = member.ssoMemberId,
                role = member.role,
                cohort = member.cohort,
                name = member.name,
                status = member.status
            )
        }
    }
}
