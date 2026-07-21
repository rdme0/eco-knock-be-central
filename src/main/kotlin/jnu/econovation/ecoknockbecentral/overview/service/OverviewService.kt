package jnu.econovation.ecoknockbecentral.overview.service

import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.service.MemberService
import jnu.econovation.ecoknockbecentral.overview.dto.request.ReplaceDefaultOverviewShortcutsRequest
import jnu.econovation.ecoknockbecentral.overview.dto.request.UpdateOverviewLayoutRequest
import jnu.econovation.ecoknockbecentral.overview.dto.request.UpdateOverviewShortcutRequest
import jnu.econovation.ecoknockbecentral.overview.dto.response.GetDefaultOverviewShortcutResponse
import jnu.econovation.ecoknockbecentral.overview.dto.response.GetOverviewShortcutResponse
import jnu.econovation.ecoknockbecentral.overview.dto.response.GetShortcutsResponse
import jnu.econovation.ecoknockbecentral.overview.exception.OverviewLayoutGridSizeConflictException
import jnu.econovation.ecoknockbecentral.overview.extension.toUserShortcut
import jnu.econovation.ecoknockbecentral.overview.model.entity.DefaultOverviewShortcut
import jnu.econovation.ecoknockbecentral.overview.model.entity.OverviewLayout
import jnu.econovation.ecoknockbecentral.overview.model.entity.OverviewShortcut
import jnu.econovation.ecoknockbecentral.overview.model.vo.GridSize
import jnu.econovation.ecoknockbecentral.overview.repository.DefaultOverviewShortcutRepository
import jnu.econovation.ecoknockbecentral.overview.repository.OverviewLayoutRepository
import jnu.econovation.ecoknockbecentral.overview.repository.OverviewShortcutRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class OverviewService(
    private val memberService: MemberService,
    private val defaultOverviewRepository: DefaultOverviewShortcutRepository,
    private val userOverviewRepository: OverviewShortcutRepository,
    private val overviewLayoutRepository: OverviewLayoutRepository,
) {
    private companion object {
        const val DEFAULT_GRID_SIZE = 3
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun initializeOverview(memberId: Long) {
        val member = memberService.getEntityOrThrow(memberId)
        if (!overviewLayoutRepository.existsByMemberId(member.id)) {
            overviewLayoutRepository.save(
                OverviewLayout.builder()
                    .member(member)
                    .gridSize(GridSize(DEFAULT_GRID_SIZE))
                    .build()
            )
        }

        val defaultOverviews = defaultOverviewRepository.findAllByOrderBySortOrderAsc()
        userOverviewRepository.deleteAllByMemberId(memberId = memberId)
        userOverviewRepository.saveAll(defaultOverviews.map { it.toUserShortcut(member) })
    }

    @Transactional
    fun updateOverviewShortcuts(
        memberInfo: MemberInfoDTO,
        updateRequest: UpdateOverviewShortcutRequest
    ) {
        val member = memberService.getEntityOrThrow(memberInfo.id)

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
    fun getOverviewShortcuts(memberInfo: MemberInfoDTO): GetShortcutsResponse {
        val member = memberService.getEntityOrThrow(memberInfo.id)
        val layout = overviewLayoutRepository.findByMemberId(member.id)
            ?: throw InternalServerException(
                IllegalStateException("id가 ${member.id}인 overview layout을 찾을 수 없음.")
            )

        return GetShortcutsResponse(
            gridSize = layout.gridSize.value,
            shortcuts = userOverviewRepository
                .findAllByMemberIdOrderBySortOrderAsc(member.id)
                .map(transform = GetOverviewShortcutResponse::from),
        )
    }

    @Transactional
    fun updateOverviewLayout(memberInfo: MemberInfoDTO, request: UpdateOverviewLayoutRequest) {
        memberService.getEntityOrThrow(memberInfo.id)
        val layout = overviewLayoutRepository.findByMemberId(memberInfo.id)
            ?: throw InternalServerException(
                IllegalStateException("id가 ${memberInfo.id}인 overview layout을 찾을 수 없음.")
            )
        if (layout.gridSize == request.gridSize) {
            throw OverviewLayoutGridSizeConflictException(request.gridSize.value)
        }

        layout.changeGridSize(request.gridSize)
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

}
