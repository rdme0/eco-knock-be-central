package jnu.econovation.ecoknockbecentral.wallet.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jnu.econovation.ecoknockbecentral.wallet.service.WalletRankingService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;

class WalletRankingSchedulerTest {

    @Test
    void delegatesRefresh() {
        WalletRankingService service = mock(WalletRankingService.class);
        WalletRankingScheduler scheduler = new WalletRankingScheduler(service);

        scheduler.refreshRanking();

        verify(service).refresh();
    }

    @Test
    void hasRequiredCronZoneAndOptInProperty() throws NoSuchMethodException {
        Scheduled scheduled = WalletRankingScheduler.class
                .getDeclaredMethod("refreshRanking")
                .getAnnotation(Scheduled.class);
        assertThat(scheduled.cron()).isEqualTo("0 0 1,4,7,10,13,16,19,22 * * *");
        assertThat(scheduled.zone()).isEqualTo("Asia/Seoul");

        ConditionalOnProperty condition = WalletRankingScheduler.class
                .getAnnotation(ConditionalOnProperty.class);
        assertThat(condition.prefix()).isEqualTo("wallet.ranking.scheduler");
        assertThat(condition.name()).containsExactly("enabled");
        assertThat(condition.havingValue()).isEqualTo("true");
        assertThat(condition.matchIfMissing()).isFalse();
    }
}
