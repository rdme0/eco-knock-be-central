package jnu.econovation.ecoknockbecentral.auth.scheduler

import jnu.econovation.ecoknockbecentral.auth.service.AuthService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class GuestMemberCleanupSchedulerTest {
    private val authService = mock<AuthService>()
    private val scheduler = GuestMemberCleanupScheduler(authService)

    @Test
    fun skipsOverlappingCleanup() {
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        val executor = Executors.newSingleThreadExecutor()

        doAnswer {
            started.countDown()
            release.await()
        }.whenever(authService).cleanupExpiredGuests()

        val runningCleanup = executor.submit { scheduler.cleanupExpiredGuests() }
        assertThat(started.await(1, TimeUnit.SECONDS)).isTrue()

        scheduler.cleanupExpiredGuests()

        verify(authService).cleanupExpiredGuests()

        release.countDown()
        runningCleanup.get(1, TimeUnit.SECONDS)
        executor.shutdownNow()
    }

    @Test
    fun releasesLockWhenCleanupFails() {
        doThrow(IllegalStateException()).doNothing()
            .whenever(authService).cleanupExpiredGuests()

        assertThatThrownBy { scheduler.cleanupExpiredGuests() }
            .isInstanceOf(IllegalStateException::class.java)

        scheduler.cleanupExpiredGuests()

        verify(authService, times(2)).cleanupExpiredGuests()
    }
}
