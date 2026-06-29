package jnu.econovation.ecoknockbecentral.overview.dto.request

import jnu.econovation.ecoknockbecentral.common.exception.client.BadDataMeaningException
import jnu.econovation.ecoknockbecentral.common.exception.client.BadDataSyntaxException
import jnu.econovation.ecoknockbecentral.overview.dto.response.GetDefaultOverviewShortcutResponse
import jnu.econovation.ecoknockbecentral.overview.model.vo.ValidHttpUrl

data class ReplaceDefaultOverviewShortcutsRequest(
    val shortcuts: List<UpdateDefaultShortcutDTO>,
) {
    companion object {
        private const val MAX_SHORTCUTS = 10
    }

    init {
        val sortOrders: Set<Int> = shortcuts.map { it.sortOrder }.toSet()

        when {
            shortcuts.size > MAX_SHORTCUTS -> {
                throw BadDataMeaningException("디폴트 바로가기는 최대 ${MAX_SHORTCUTS}개까지 등록할 수 있습니다.")
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

data class UpdateDefaultShortcutDTO(
    val iconUrl: ValidHttpUrl,
    val targetUrl: ValidHttpUrl,
    val name: String,
    val sortOrder: Int,
) {
    companion object {
        private const val MAX_NAME_LENGTH = 10

        fun from(response: GetDefaultOverviewShortcutResponse): UpdateDefaultShortcutDTO {
            return UpdateDefaultShortcutDTO(
                iconUrl = ValidHttpUrl(response.iconUrl),
                targetUrl = ValidHttpUrl(response.targetUrl),
                name = response.name,
                sortOrder = response.sortOrder,
            )
        }
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
