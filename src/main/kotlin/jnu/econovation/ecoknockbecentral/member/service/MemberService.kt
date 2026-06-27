package jnu.econovation.ecoknockbecentral.member.service

import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.model.entity.Member
import jnu.econovation.ecoknockbecentral.member.repository.MemberRepository
import jnu.econovation.ecoknockbecentral.sso.dto.SSOMeDTO
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class MemberService(
    private val repository: MemberRepository
) {
    @Transactional
    fun save(member: Member) = repository.save(member)

    @Transactional
    fun getOrSaveFromSso(dto: SSOMeDTO): MemberInfoDTO {
        val existing = repository.findBySsoMemberId(dto.ssoMemberId)
        if (existing != null) {
            return MemberInfoDTO.from(existing)
        }

        val member = Member.builder()
            .ssoMemberId(dto.ssoMemberId)
            .cohort(dto.cohort)
            .name(dto.name)
            .status(dto.status)
            .build()

        return MemberInfoDTO.from(repository.save(member))
    }

    @Transactional(readOnly = true)
    fun get(id: Long): MemberInfoDTO? {
        val entity = getEntity(id) ?: return null

        return MemberInfoDTO.from(entity)
    }

    @Transactional(readOnly = true)
    fun getEntity(id: Long): Member? = repository.findById(id).getOrNull()

    @Transactional(readOnly = true)
    fun getEntityOrThrow(id: Long): Member {
        return getEntity(id) ?: throw InternalServerException(
            IllegalStateException("id가 ${id}인 회원을 찾을 수 없음.")
        )
    }
}
