package jnu.econovation.ecoknockbecentral.common.openapi

import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class ApiDocAccessService(
    private val repository: ApiDocAccessRepository,
) {
    private val logger = KotlinLogging.logger {}

    fun isEnabled(): Boolean {
        return runCatching { repository.isEnabled() }
            .onFailure { logger.warn(it) { "API 문서 접근 설정 조회 실패" } }
            .getOrDefault(false)
    }

    fun update(enabled: Boolean): ApiDocAccessResponse {
        repository.save(enabled)
        return ApiDocAccessResponse(enabled = enabled)
    }
}

data class ApiDocAccessResponse(
    val enabled: Boolean,
)

data class UpdateApiDocAccessRequest(
    val enabled: Boolean,
)
