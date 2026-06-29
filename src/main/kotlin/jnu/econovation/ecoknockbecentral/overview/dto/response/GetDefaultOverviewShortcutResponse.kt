package jnu.econovation.ecoknockbecentral.overview.dto.response

import jnu.econovation.ecoknockbecentral.overview.model.entity.DefaultOverviewShortcut

data class GetDefaultOverviewShortcutResponse(
    val iconUrl: String,
    val targetUrl: String,
    val sortOrder: Int,
    val name: String
) {
    companion object {
        fun from(entity: DefaultOverviewShortcut): GetDefaultOverviewShortcutResponse {
            return GetDefaultOverviewShortcutResponse(
                iconUrl = entity.iconUrl.value,
                targetUrl = entity.targetUrl.value,
                sortOrder = entity.sortOrder,
                name = entity.name
            )
        }
    }
}
