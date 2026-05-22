package jnu.econovation.ecoknockbecentral.overview.service

import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.service.MemberService
import jnu.econovation.ecoknockbecentral.overview.dto.request.UpdateOverviewShortcutRequest
import jnu.econovation.ecoknockbecentral.overview.dto.response.GetOverviewShortcutResponse
import jnu.econovation.ecoknockbecentral.overview.extension.toUserShortcut
import jnu.econovation.ecoknockbecentral.overview.model.entity.OverviewShortcut
import jnu.econovation.ecoknockbecentral.overview.repository.DefaultOverviewShortcutRepository
import jnu.econovation.ecoknockbecentral.overview.repository.OverviewShortcutRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OverviewService(
    private val memberService: MemberService,
    private val defaultOverviewRepository: DefaultOverviewShortcutRepository,
    private val userOverviewRepository: OverviewShortcutRepository
) {

    @Transactional
    fun initOverviewShortcuts(memberInfo: MemberInfoDTO) {
        // todo: 회원가입 시 init 하도록
        val member = memberService.getEntityOrThrow(id = memberInfo.id)
        val defaultOverviews = defaultOverviewRepository.findAll()

        userOverviewRepository.deleteAllByMemberId(memberId = member.id)
        userOverviewRepository.saveAll(defaultOverviews.map { it.toUserShortcut(member) })
    }

    @Transactional
    fun updateOverviewShortcuts(
        memberInfo: MemberInfoDTO,
        updateRequest: UpdateOverviewShortcutRequest
    ) {
        val member = memberService.getEntityOrThrow(id = memberInfo.id)

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
        val member = memberService.getEntityOrThrow(id = memberInfo.id)

        return userOverviewRepository
            .findAllByMemberIdOrderBySortOrderAsc(member.id)
            .map(transform = GetOverviewShortcutResponse::from)
    }

}