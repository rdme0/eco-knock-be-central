package jnu.econovation.ecoknockbecentral.overview.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import jnu.econovation.ecoknockbecentral.overview.model.entity.DefaultOverviewShortcut

data class GetDefaultOverviewShortcutResponse(
    @field:Schema(
        description = "아이콘 이미지 URL. 없으면 null",
        example = "https://example.com/icon.png",
        nullable = true,
    )
    val iconUrl: String?,
    val targetUrl: String,
    val sortOrder: Int,
    val name: String
) {
    companion object {
        fun from(entity: DefaultOverviewShortcut): GetDefaultOverviewShortcutResponse {
            return GetDefaultOverviewShortcutResponse(
                iconUrl = entity.iconUrl?.value,
                targetUrl = entity.targetUrl.value,
                sortOrder = entity.sortOrder,
                name = entity.name
            )
        }
    }
}
