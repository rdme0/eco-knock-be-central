package jnu.econovation.ecoknockbecentral.reward.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import jnu.econovation.ecoknockbecentral.reward.client.RewardDistributorClient;
import jnu.econovation.ecoknockbecentral.reward.dto.RewardRecipient;
import jnu.econovation.ecoknockbecentral.reward.dto.RewardSettlementResult;
import jnu.econovation.ecoknockbecentral.reward.dto.RewardTransactionResult;
import jnu.econovation.ecoknockbecentral.reward.exception.RewardTransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.web3j.crypto.Hash;

class RewardTransactionServiceTest {

    private static final LocalDate SETTLEMENT_DATE = LocalDate.of(2026, 7, 21);
    private static final String FIRST_WALLET = "0x1111111111111111111111111111111111111111";
    private static final String SECOND_WALLET = "0x2222222222222222222222222222222222222222";
    private static final String TRANSACTION_HASH = "0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    private static final BigInteger TOKEN_UNIT = BigInteger.TEN.pow(18);

    private RewardDistributorClient rewardDistributorClient;
    private RewardTransactionService service;

    @BeforeEach
    void setUp() {
        rewardDistributorClient = mock(RewardDistributorClient.class);
        service = new RewardTransactionService(rewardDistributorClient);
    }

    @Test
    @SuppressWarnings("unchecked")
    void convertsSettlementAndSubmitsRewardTransaction() {
        RewardSettlementResult settlement = new RewardSettlementResult(
                SETTLEMENT_DATE,
                List.of(
                        new RewardRecipient(FIRST_WALLET, 5L),
                        new RewardRecipient(SECOND_WALLET, 7L)
                ),
                12L
        );
        when(rewardDistributorClient.submit(anyString(), any(), anyList(), anyList()))
                .thenReturn(TRANSACTION_HASH);

        Optional<RewardTransactionResult> result = service.submit(settlement);

        ArgumentCaptor<String> batchIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BigInteger> rewardDayCaptor = ArgumentCaptor.forClass(BigInteger.class);
        ArgumentCaptor<List<String>> recipientsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<BigInteger>> amountsCaptor = ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(rewardDistributorClient).submit(
                batchIdCaptor.capture(),
                rewardDayCaptor.capture(),
                recipientsCaptor.capture(),
                amountsCaptor.capture()
        );

        String expectedBatchId = Hash.sha3String("eco-knock:daily-reward:2026-07-21");
        assertThat(batchIdCaptor.getValue()).isEqualTo(expectedBatchId);
        assertThat(rewardDayCaptor.getValue()).isEqualTo(new BigInteger("20260721"));
        assertThat(recipientsCaptor.getValue()).containsExactly(FIRST_WALLET, SECOND_WALLET);
        assertThat(amountsCaptor.getValue()).containsExactly(
                TOKEN_UNIT.multiply(BigInteger.valueOf(5L)),
                TOKEN_UNIT.multiply(BigInteger.valueOf(7L))
        );
        assertThat(result).contains(new RewardTransactionResult(
                expectedBatchId,
                new BigInteger("20260721"),
                TRANSACTION_HASH
        ));
    }

    @Test
    void skipsTransactionWhenSettlementHasNoRecipients() {
        RewardSettlementResult settlement = new RewardSettlementResult(
                SETTLEMENT_DATE,
                List.of(),
                0L
        );

        Optional<RewardTransactionResult> result = service.submit(settlement);

        assertThat(result).isEmpty();
        verifyNoInteractions(rewardDistributorClient);
    }

    @Test
    void rejectsSettlementWithMismatchedTotal() {
        RewardSettlementResult settlement = new RewardSettlementResult(
                SETTLEMENT_DATE,
                List.of(new RewardRecipient(FIRST_WALLET, 5L)),
                6L
        );

        assertThatThrownBy(() -> service.submit(settlement))
                .isInstanceOf(RewardTransactionException.class)
                .hasRootCauseMessage("Reward settlement total does not match recipients");
        verifyNoInteractions(rewardDistributorClient);
    }

    @Test
    void delegatesTransactionConfirmation() {
        when(rewardDistributorClient.waitForConfirmation(TRANSACTION_HASH)).thenReturn(true);

        boolean confirmed = service.waitForConfirmation(TRANSACTION_HASH);

        assertThat(confirmed).isTrue();
    }

    @Test
    void delegatesTransactionHashRecovery() {
        String batchId = service.createBatchId(SETTLEMENT_DATE);
        when(rewardDistributorClient.findTransactionHashByBatchId(batchId))
                .thenReturn(Optional.of(TRANSACTION_HASH));

        Optional<String> recovered = service.findTransactionHashByBatchId(batchId);

        assertThat(recovered).contains(TRANSACTION_HASH);
    }
}
