package jnu.econovation.ecoknockbecentral.reward.scheduler;

import java.time.Clock;
import java.time.LocalDate;
import jnu.econovation.ecoknockbecentral.reward.service.RewardDistributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "reward.scheduler", name = "enabled", havingValue = "true")
public class RewardDistributionScheduler {

    private final RewardDistributionService rewardDistributionService;
    private final Clock rewardSchedulerClock;

    @Scheduled(cron = "0 10,20,30 0 * * *", zone = "Asia/Seoul")
    public void distributePreviousDayRewards() {
        LocalDate settlementDate = LocalDate.now(rewardSchedulerClock).minusDays(1);
        rewardDistributionService.distribute(settlementDate);
    }
}
