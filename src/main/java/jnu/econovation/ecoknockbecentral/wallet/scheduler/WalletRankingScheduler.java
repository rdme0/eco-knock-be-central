package jnu.econovation.ecoknockbecentral.wallet.scheduler;

import jnu.econovation.ecoknockbecentral.wallet.service.WalletRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "wallet.ranking.scheduler", name = "enabled", havingValue = "true")
public class WalletRankingScheduler {

    private final WalletRankingService walletRankingService;

    @Scheduled(cron = "0 0 1,4,7,10,13,16,19,22 * * *", zone = "Asia/Seoul")
    public void refreshRanking() {
        walletRankingService.refresh();
    }
}
