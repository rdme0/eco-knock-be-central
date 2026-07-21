package jnu.econovation.ecoknockbecentral.overview.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class GetShortcutsResponse(
    @field:Schema(
        type = "integer",
        description = "모아두기 그리드 열 수",
        allowableValues = ["2", "3"],
        example = "3",
    )
    val gridSize: Int,
    val shortcuts: List<GetOverviewShortcutResponse>,
)
