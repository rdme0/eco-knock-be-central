package jnu.econovation.ecoknockbecentral.wallet.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import jnu.econovation.ecoknockbecentral.member.model.vo.Role;
import jnu.econovation.ecoknockbecentral.wallet.client.KrtBalanceClient;
import jnu.econovation.ecoknockbecentral.wallet.dto.WalletRankingDTO;
import jnu.econovation.ecoknockbecentral.wallet.dto.WalletRankingEntryDTO;
import jnu.econovation.ecoknockbecentral.wallet.dto.request.WalletRankingRequest;
import jnu.econovation.ecoknockbecentral.wallet.dto.response.WalletRankingResponse;
import jnu.econovation.ecoknockbecentral.wallet.model.entity.MemberWallet;
import jnu.econovation.ecoknockbecentral.wallet.repository.WalletRankingCacheRepository;
import jnu.econovation.ecoknockbecentral.wallet.repository.MemberWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletRankingService {

    private static final int KRT_DECIMALS = 18;
    private static final String KRT_SYMBOL = "KRT";

    private final MemberWalletRepository memberWalletRepository;
    private final KrtBalanceClient krtBalanceClient;
    private final WalletRankingCacheRepository walletRankingCacheRepository;

    public WalletRankingResponse getRankings(WalletRankingRequest request) {
        WalletRankingDTO snapshot = walletRankingCacheRepository.findCurrent().orElse(null);
        if (snapshot == null) {
            snapshot = calculateAndCache();
        }
        return snapshot.toResponse(request.limit());
    }

    public void refresh() {
        calculateAndCache();
    }

    private WalletRankingDTO calculateAndCache() {
        List<RankingCandidate> candidates = memberWalletRepository
                .findActiveRewardDestinationsExcludingRole(Role.GUEST)
                .stream()
                .map(wallet -> new RankingCandidate(
                        wallet,
                        krtBalanceClient.getBalance(wallet.getWalletAddress())
                ))
                .filter(candidate -> candidate.rawBalance().signum() > 0)
                .sorted(Comparator
                        .comparing(RankingCandidate::rawBalance, Comparator.reverseOrder())
                        .thenComparing(candidate -> candidate.wallet().getMember().getId()))
                .toList();

        List<WalletRankingEntryDTO> rankings = IntStream.range(0, candidates.size())
                .mapToObj(index -> {
                    RankingCandidate candidate = candidates.get(index);
                    Member member = candidate.wallet().getMember();
                    String balance = new BigDecimal(candidate.rawBalance(), KRT_DECIMALS)
                            .stripTrailingZeros()
                            .toPlainString();
                    return new WalletRankingEntryDTO(
                            index + 1,
                            member.getId(),
                            member.getName(),
                            member.getCohort().value(),
                            balance
                    );
                })
                .toList();
        WalletRankingDTO snapshot = new WalletRankingDTO(KRT_SYMBOL, Instant.now(), rankings);

        walletRankingCacheRepository.save(snapshot);
        return snapshot;
    }

    private record RankingCandidate(MemberWallet wallet, BigInteger rawBalance) {
    }
}
