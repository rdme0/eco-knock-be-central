package jnu.econovation.ecoknockbecentral.auth.scheduler

import jnu.econovation.ecoknockbecentral.auth.service.AuthService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicBoolean

@Component
class GuestMemberCleanupScheduler(
    private val authService: AuthService,
) {
    private val isRunning = AtomicBoolean(false)

    @Scheduled(cron = "0 */5 * * * *")
    fun cleanupExpiredGuests() {
        if (!isRunning.compareAndSet(false, true)) {
            return
        }

        try {
            authService.cleanupExpiredGuests()
        } finally {
            isRunning.set(false)
        }
    }
}
