package jnu.econovation.ecoknockbecentral.overview.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import jnu.econovation.ecoknockbecentral.overview.model.entity.OverviewShortcut

data class GetOverviewShortcutResponse(
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
        fun from(entity: OverviewShortcut): GetOverviewShortcutResponse {
            return GetOverviewShortcutResponse(
                iconUrl = entity.iconUrl?.value,
                targetUrl = entity.targetUrl.value,
                sortOrder = entity.sortOrder,
                name = entity.name
            )
        }
    }
}
