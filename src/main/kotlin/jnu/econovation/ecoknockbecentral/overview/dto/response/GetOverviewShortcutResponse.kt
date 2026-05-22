package jnu.econovation.ecoknockbecentral.overview.dto.response

import jnu.econovation.ecoknockbecentral.overview.model.entity.OverviewShortcut

typealias GetShortcutsResponse = List<GetOverviewShortcutResponse>

data class GetOverviewShortcutResponse(
    val iconUrl: String,
    val targetUrl: String,
    val sortOrder: Int,
    val name: String
) {
    companion object {
        fun from(entity: OverviewShortcut): GetOverviewShortcutResponse {
            return GetOverviewShortcutResponse(
                iconUrl = entity.iconUrl.value,
                targetUrl = entity.targetUrl.value,
                sortOrder = entity.sortOrder,
                name = entity.name
            )
        }
    }
}