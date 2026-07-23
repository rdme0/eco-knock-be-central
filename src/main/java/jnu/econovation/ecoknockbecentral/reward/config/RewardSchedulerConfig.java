package jnu.econovation.ecoknockbecentral.reward.config;

import java.time.Clock;
import jnu.econovation.ecoknockbecentral.common.util.TimeUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RewardSchedulerConfig {

    @Bean
    public Clock rewardSchedulerClock() {
        return Clock.system(TimeUtil.SEOUL_ZONE_ID);
    }
}
