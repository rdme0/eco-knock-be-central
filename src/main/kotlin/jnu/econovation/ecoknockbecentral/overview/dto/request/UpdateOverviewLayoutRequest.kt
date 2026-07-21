package jnu.econovation.ecoknockbecentral.overview.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jnu.econovation.ecoknockbecentral.overview.model.vo.GridSize

data class UpdateOverviewLayoutRequest(
    @field:Schema(
        type = "integer",
        description = "모아두기 그리드 열 수. 2 또는 3",
        allowableValues = ["2", "3"],
        example = "2",
    )
    val gridSize: GridSize,
)
