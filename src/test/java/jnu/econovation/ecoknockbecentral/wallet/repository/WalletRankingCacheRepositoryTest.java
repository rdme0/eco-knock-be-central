package jnu.econovation.ecoknockbecentral.wallet.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import jnu.econovation.ecoknockbecentral.wallet.dto.WalletRankingDTO;
import jnu.econovation.ecoknockbecentral.wallet.dto.WalletRankingEntryDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.json.JsonMapper;

class WalletRankingCacheRepositoryTest {

    @Test
    @SuppressWarnings("unchecked")
    void serializesAndReadsCompleteSnapshotUsingNonExpiringRedisSet() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        WalletRankingCacheRepository repository = new WalletRankingCacheRepository(
                redisTemplate,
                JsonMapper.builder().findAndAddModules().build()
        );
        WalletRankingDTO snapshot = new WalletRankingDTO(
                "KRT",
                Instant.parse("2026-07-23T01:00:00Z"),
                List.of(new WalletRankingEntryDTO(1, 7L, "member", 32, "13.5"))
        );

        repository.save(snapshot);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq(WalletRankingCacheRepository.CACHE_KEY), jsonCaptor.capture());
        when(valueOperations.get(WalletRankingCacheRepository.CACHE_KEY))
                .thenReturn(jsonCaptor.getValue());
        assertThat(repository.findCurrent()).contains(snapshot);
    }
}
