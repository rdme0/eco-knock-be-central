package jnu.econovation.ecoknockbecentral.overview.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jnu.econovation.ecoknockbecentral.common.exception.client.BadDataMeaningException
import jnu.econovation.ecoknockbecentral.common.exception.client.BadDataSyntaxException
import jnu.econovation.ecoknockbecentral.overview.model.vo.ValidHttpUrl

data class UpdateOverviewShortcutRequest(
    @field:Schema(description = "저장할 바로가기 목록. 최대 20개")
    val shortcuts: List<UpdateShortcutDTO>
) {
    companion object {
        private const val MAX_SHORTCUTS = 20
    }

    init {
        val sortOrders: Set<Int> = shortcuts.map { it.sortOrder }.toSet()
        when {
            shortcuts.size > MAX_SHORTCUTS -> {
                throw BadDataMeaningException("바로가기는 최대 ${MAX_SHORTCUTS}개까지 등록할 수 있습니다.")
            }

            shortcuts.size != sortOrders.size -> {
                throw BadDataMeaningException("sortOrder는 중복될 수 없습니다.")
            }

            sortOrders != shortcuts.indices.toSet() -> {
                throw BadDataMeaningException("sortOrder는 0부터 ${shortcuts.size - 1}까지 빠짐없이 포함해야 합니다.")
            }
        }
    }
}

data class UpdateShortcutDTO(
    @field:Schema(description = "아이콘 이미지 URL. 없으면 null", example = "https://example.com/icon.png", nullable = true)
    val iconUrl: ValidHttpUrl?,
    @field:Schema(description = "이동 대상 URL. http 또는 https만 허용", example = "https://example.com")
    val targetUrl: ValidHttpUrl,
    @field:Schema(description = "정렬 순서. 0부터 N-1까지 중복 없이 연속되어야 함", minimum = "0", example = "0")
    val sortOrder: Int,
    @field:Schema(description = "바로가기 이름. 공백 불가, 최대 10자", example = "홈페이지")
    val name: String
) {
    companion object {
        private const val MAX_NAME_LENGTH = 10
    }

    init {
        when {
            name.isBlank() -> {
                throw BadDataSyntaxException("바로가기 이름은 필수입니다.")
            }

            name.length > MAX_NAME_LENGTH -> {
                throw BadDataMeaningException("바로가기 이름은 ${MAX_NAME_LENGTH}자를 초과할 수 없습니다.")
            }

            sortOrder < 0 -> {
                throw BadDataSyntaxException("sortOrder는 0보다 작을 수 없습니다.")
            }
        }
    }
}
