package jnu.econovation.ecoknockbecentral.member.event

import jnu.econovation.ecoknockbecentral.member.model.vo.Role

data class MemberCreatedEvent(
    val memberId: Long,
    val role: Role
)
