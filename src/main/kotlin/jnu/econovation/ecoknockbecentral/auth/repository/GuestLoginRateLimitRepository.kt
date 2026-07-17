package jnu.econovation.ecoknockbecentral.auth.repository

import jnu.econovation.ecoknockbecentral.auth.config.AuthPolicyConfig
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Repository

@Repository
class GuestLoginRateLimitRepository(
    private val redisTemplate: StringRedisTemplate,
    private val authPolicyConfig: AuthPolicyConfig,
) {
    companion object {
        private const val KEY_PREFIX = "auth:guest-login-rate-limit:"

        private val INCREMENT_SCRIPT = DefaultRedisScript<Long>().apply {
            setLocation(ClassPathResource("redis/increment-guest-login-rate-limit.lua"))
            resultType = Long::class.java
        }
    }

    fun tryAcquire(clientIp: String): Boolean {
        val count = redisTemplate.execute(
            INCREMENT_SCRIPT,
            listOf(key(clientIp)),
            authPolicyConfig.guestLoginRateLimitWindow.toMillis().toString(),
        ) ?: return false

        return count <= authPolicyConfig.guestLoginRateLimit
    }

    private fun key(clientIp: String): String {
        return "$KEY_PREFIX$clientIp"
    }
}
