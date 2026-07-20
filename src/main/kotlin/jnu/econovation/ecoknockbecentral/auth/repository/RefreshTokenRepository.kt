package jnu.econovation.ecoknockbecentral.auth.repository

import jnu.econovation.ecoknockbecentral.auth.config.AuthPolicyConfig
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Repository
import java.time.Duration


enum class RefreshTokenRotationResult {
    SUCCESSFULLY_REPLACED,
    MISSING,
    MISMATCHED,
    FAILED,
}

@Repository
class RefreshTokenRepository(
    private val redisTemplate: StringRedisTemplate,
    private val authPolicyConfig: AuthPolicyConfig,
) {
    companion object {
        private const val KEY_PREFIX = "auth:refresh:"

        private const val SUCCESSFULLY_REPLACED_CODE = 1L
        private const val MISSING_CODE = 0L
        private const val MISMATCHED_CODE = -1L
        private const val DELETED_CODE = 1L

        private val ROTATE_SCRIPT = DefaultRedisScript<Long>().apply {
            setLocation(ClassPathResource("redis/rotate-refresh-token.lua"))
            resultType = Long::class.java
        }

        private val DELETE_IF_MATCHES_SCRIPT = DefaultRedisScript<Long>().apply {
            setLocation(ClassPathResource("redis/delete-refresh-token-if-matches.lua"))
            resultType = Long::class.java
        }
    }

    fun save(memberId: Long, tokenId: String, ttl: Duration) {
        redisTemplate.opsForValue().set(
            key(memberId),
            tokenId,
            ttl,
        )
    }

    fun replaceIfMatches(
        memberId: Long,
        currentTokenId: String,
        nextTokenId: String,
        ttl: Duration = authPolicyConfig.refreshTokenTTL,
    ): RefreshTokenRotationResult {
        val result = redisTemplate.execute(
            ROTATE_SCRIPT,
            listOf(key(memberId)),
            currentTokenId,
            nextTokenId,
            ttl.toMillis().toString(),
        )

        return when (result) {
            SUCCESSFULLY_REPLACED_CODE -> RefreshTokenRotationResult.SUCCESSFULLY_REPLACED
            MISSING_CODE -> RefreshTokenRotationResult.MISSING
            MISMATCHED_CODE -> RefreshTokenRotationResult.MISMATCHED
            else -> RefreshTokenRotationResult.FAILED
        }
    }

    fun delete(memberId: Long) {
        redisTemplate.delete(key(memberId))
    }

    fun deleteIfMatches(memberId: Long, tokenId: String): Boolean {
        return redisTemplate.execute(
            DELETE_IF_MATCHES_SCRIPT,
            listOf(key(memberId)),
            tokenId,
        ) == DELETED_CODE
    }

    private fun key(memberId: Long): String {
        return "$KEY_PREFIX$memberId"
    }
}
