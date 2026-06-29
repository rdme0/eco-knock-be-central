package jnu.econovation.ecoknockbecentral.overview.service

import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.model.entity.Member
import jnu.econovation.ecoknockbecentral.member.repository.MemberRepository
import jnu.econovation.ecoknockbecentral.overview.dto.request.ReplaceDefaultOverviewShortcutsRequest
import jnu.econovation.ecoknockbecentral.overview.dto.request.UpdateOverviewShortcutRequest
import jnu.econovation.ecoknockbecentral.overview.dto.response.GetDefaultOverviewShortcutResponse
import jnu.econovation.ecoknockbecentral.overview.dto.response.GetOverviewShortcutResponse
import jnu.econovation.ecoknockbecentral.overview.extension.toUserShortcut
import jnu.econovation.ecoknockbecentral.overview.model.entity.DefaultOverviewShortcut
import jnu.econovation.ecoknockbecentral.overview.model.entity.OverviewShortcut
import jnu.econovation.ecoknockbecentral.overview.repository.DefaultOverviewShortcutRepository
import jnu.econovation.ecoknockbecentral.overview.repository.OverviewShortcutRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class OverviewService(
    private val memberRepository: MemberRepository,
    private val defaultOverviewRepository: DefaultOverviewShortcutRepository,
    private val userOverviewRepository: OverviewShortcutRepository
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun initOverviewShortcuts(memberId: Long) {
        val member = getMemberOrThrow(memberId)
        val defaultOverviews = defaultOverviewRepository.findAllByOrderBySortOrderAsc()

        userOverviewRepository.deleteAllByMemberId(memberId = memberId)
        userOverviewRepository.saveAll(defaultOverviews.map { it.toUserShortcut(member) })
    }

    @Transactional
    fun updateOverviewShortcuts(
        memberInfo: MemberInfoDTO,
        updateRequest: UpdateOverviewShortcutRequest
    ) {
        val member = getMemberOrThrow(memberInfo.id)

        userOverviewRepository.deleteAllByMemberId(memberId = memberInfo.id)

        val entities = updateRequest.shortcuts.map {
            OverviewShortcut.builder()
                .name(it.name)
                .member(member)
                .targetUrl(it.targetUrl)
                .iconUrl(it.iconUrl)
                .sortOrder(it.sortOrder)
                .build()
        }

        userOverviewRepository.saveAll(entities)
    }

    @Transactional(readOnly = true)
    fun getOverviewShortcuts(memberInfo: MemberInfoDTO): List<GetOverviewShortcutResponse> {
        val member = getMemberOrThrow(memberInfo.id)

        return userOverviewRepository
            .findAllByMemberIdOrderBySortOrderAsc(member.id)
            .map(transform = GetOverviewShortcutResponse::from)
    }

    @Transactional(readOnly = true)
    fun getDefaultOverviewShortcuts(): List<GetDefaultOverviewShortcutResponse> {
        return defaultOverviewRepository
            .findAllByOrderBySortOrderAsc()
            .map(transform = GetDefaultOverviewShortcutResponse::from)
    }

    @Transactional
    fun replaceDefaultOverviewShortcuts(updateRequest: ReplaceDefaultOverviewShortcutsRequest) {
        defaultOverviewRepository.deleteAllInBatch()

        val entities = updateRequest.shortcuts.map {
            DefaultOverviewShortcut.builder()
                .name(it.name)
                .targetUrl(it.targetUrl)
                .iconUrl(it.iconUrl)
                .sortOrder(it.sortOrder)
                .build()
        }

        defaultOverviewRepository.saveAll(entities)
    }

    private fun getMemberOrThrow(memberId: Long): Member {
        return memberRepository.findById(memberId).getOrNull()
            ?: throw InternalServerException(
                IllegalStateException("id가 ${memberId}인 회원을 찾을 수 없음.")
            )
    }

}
