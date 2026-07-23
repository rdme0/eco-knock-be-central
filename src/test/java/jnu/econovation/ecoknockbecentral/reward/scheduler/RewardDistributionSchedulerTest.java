package jnu.econovation.ecoknockbecentral.reward.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import jnu.econovation.ecoknockbecentral.reward.service.RewardDistributionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;

class RewardDistributionSchedulerTest {

    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");

    @Test
    void distributesRewardsForPreviousCalendarDateInSeoul() {
        RewardDistributionService rewardDistributionService = mock(
                RewardDistributionService.class
        );
        Clock clock = Clock.fixed(Instant.parse("2026-07-22T15:10:00Z"), SEOUL_ZONE_ID);
        RewardDistributionScheduler scheduler = new RewardDistributionScheduler(
                rewardDistributionService,
                clock
        );

        scheduler.distributePreviousDayRewards();

        verify(rewardDistributionService).distribute(LocalDate.of(2026, 7, 22));
    }

    @Test
    void runsAtConfiguredTimesInSeoul() throws NoSuchMethodException {
        Scheduled scheduled = RewardDistributionScheduler.class
                .getDeclaredMethod("distributePreviousDayRewards")
                .getAnnotation(Scheduled.class);

        assertThat(scheduled).isNotNull();
        assertThat(scheduled.cron()).isEqualTo("0 10,20,30 0 * * *");
        assertThat(scheduled.zone()).isEqualTo("Asia/Seoul");
    }

    @Test
    void isCreatedOnlyWhenSchedulerIsExplicitlyEnabled() {
        ConditionalOnProperty condition = RewardDistributionScheduler.class
                .getAnnotation(ConditionalOnProperty.class);

        assertThat(condition).isNotNull();
        assertThat(condition.prefix()).isEqualTo("reward.scheduler");
        assertThat(condition.name()).containsExactly("enabled");
        assertThat(condition.havingValue()).isEqualTo("true");
        assertThat(condition.matchIfMissing()).isFalse();
    }
}
