package jnu.econovation.ecoknockbecentral.member.dto.response

import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort
import jnu.econovation.ecoknockbecentral.member.model.vo.Role

data class GetProfileResponse(
    val role: Role,
    val cohort: Cohort?,
    val name: String,
    val activeStatus: ActiveStatus?
) {
    companion object {
        fun from(memberInfo: MemberInfoDTO): GetProfileResponse {
            return GetProfileResponse(
                role = memberInfo.role,
                cohort = memberInfo.cohort,
                name = memberInfo.name,
                activeStatus = memberInfo.status
            )
        }
    }
}