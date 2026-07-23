package jnu.econovation.ecoknockbecentral.wallet.repository;

import java.util.Optional;
import jnu.econovation.ecoknockbecentral.wallet.dto.WalletRankingDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

@Repository
@RequiredArgsConstructor
public class WalletRankingCacheRepository {

    static final String CACHE_KEY = "wallet:ranking:current";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public Optional<WalletRankingDTO> findCurrent() {
        String serialized = redisTemplate.opsForValue().get(CACHE_KEY);
        if (serialized == null) {
            return Optional.empty();
        }
        return Optional.of(objectMapper.readValue(serialized, WalletRankingDTO.class));
    }

    public void save(WalletRankingDTO snapshot) {
        redisTemplate.opsForValue().set(CACHE_KEY, objectMapper.writeValueAsString(snapshot));
    }
}
