package jnu.econovation.ecoknockbecentral.member.repository

import jnu.econovation.ecoknockbecentral.member.model.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import jnu.econovation.ecoknockbecentral.member.model.vo.Role

@Repository
interface MemberRepository : JpaRepository<Member, Long> {
    fun findBySsoMemberId(ssoMemberId: Long): Member?

    fun findAllByRoleAndGuestExpiresAtLessThanEqual(role: Role, guestExpiresAt: Instant): List<Member>
}
