package jnu.econovation.ecoknockbecentral.common.openapi.repository

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository

@Repository
class ApiDocAccessRepository(
    private val redisTemplate: StringRedisTemplate,
) {
    companion object {
        private const val KEY = "admin:api-docs:enabled"
        private const val ENABLED = "true"
        private const val DISABLED = "false"
    }

    fun isEnabled(): Boolean {
        return redisTemplate.opsForValue().get(KEY) == ENABLED
    }

    fun save(enabled: Boolean) {
        redisTemplate.opsForValue().set(KEY, if (enabled) ENABLED else DISABLED)
    }
}
