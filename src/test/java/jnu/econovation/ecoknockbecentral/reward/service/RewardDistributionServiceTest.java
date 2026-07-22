package jnu.econovation.ecoknockbecentral.reward.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jnu.econovation.ecoknockbecentral.reward.dto.RewardRecipient;
import jnu.econovation.ecoknockbecentral.reward.dto.RewardSettlementResult;
import jnu.econovation.ecoknockbecentral.reward.dto.RewardTransactionDTO;
import jnu.econovation.ecoknockbecentral.reward.exception.RewardSubmissionUnknownException;
import jnu.econovation.ecoknockbecentral.reward.exception.RewardTransactionException;
import jnu.econovation.ecoknockbecentral.reward.model.entity.RewardDistribution;
import jnu.econovation.ecoknockbecentral.reward.model.vo.RewardDistributionStatus;
import jnu.econovation.ecoknockbecentral.reward.repository.RewardDistributionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@SuppressWarnings("unchecked")
class RewardDistributionServiceTest {

    private static final LocalDate SETTLEMENT_DATE = LocalDate.of(2026, 7, 21);
    private static final String BATCH_ID =
            "0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    private static final String TRANSACTION_HASH =
            "0xbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
    private static final String WALLET_ADDRESS =
            "0x1111111111111111111111111111111111111111";
    private static final BigInteger REWARD_DAY = new BigInteger("20260721");

    private RewardSettlementService settlementService;
    private RewardTransactionService transactionService;
    private RewardDistributionRepository repository;
    private RewardDistributionService service;

    @BeforeEach
    void setUp() {
        settlementService = mock(RewardSettlementService.class);
        transactionService = mock(RewardTransactionService.class);
        repository = mock(RewardDistributionRepository.class);
        service = new RewardDistributionService(
                settlementService,
                transactionService,
                repository
        );

        when(repository.saveAndFlush(any(RewardDistribution.class)))
                .thenAnswer(invocation -> assignId(invocation.getArgument(0)));
        when(repository.claimForSubmission(any(), anyLong(), anyInt(), any(), any()))
                .thenReturn(1);
        when(transactionService.createBatchId(SETTLEMENT_DATE)).thenReturn(BATCH_ID);
    }

    @Test
    void persistsSubmittedHashBeforeConfirmingDistribution() {
        List<RewardDistributionStatus> savedStatuses = captureSavedStatuses();
        RewardSettlementResult settlement = settlement();
        RewardTransactionDTO submitted = transactionDTO();
        when(settlementService.settle(SETTLEMENT_DATE)).thenReturn(settlement);
        when(repository.findByBatchId(BATCH_ID)).thenReturn(Optional.empty());
        when(transactionService.submit(settlement)).thenReturn(Optional.of(submitted));
        when(transactionService.waitForConfirmation(TRANSACTION_HASH)).thenReturn(true);

        Optional<RewardTransactionDTO> result = service.distribute(SETTLEMENT_DATE);

        assertThat(result).contains(submitted);
        assertThat(savedStatuses).containsExactly(
                RewardDistributionStatus.PENDING,
                RewardDistributionStatus.SUBMITTED,
                RewardDistributionStatus.CONFIRMED
        );
    }

    @Test
    void keepsSubmittedStateWhenConfirmationPollingFails() {
        RewardSettlementResult settlement = settlement();
        RewardDistribution distribution = pendingDistribution();
        distribution.markSubmitting(5L, 1);
        distribution.markSubmitted(TRANSACTION_HASH);
        when(repository.findByBatchId(BATCH_ID)).thenReturn(Optional.of(distribution));
        RewardTransactionException pollingFailure = failure("polling failed");
        when(transactionService.waitForConfirmation(TRANSACTION_HASH))
                .thenThrow(pollingFailure);

        assertThatThrownBy(() -> service.distribute(SETTLEMENT_DATE))
                .isSameAs(pollingFailure);
        assertThat(distribution.getStatus()).isEqualTo(RewardDistributionStatus.SUBMITTED);
        verify(transactionService, never()).submit(any());
        verifyNoInteractions(settlementService);
    }

    @Test
    void marksDistributionFailedWhenSubmissionFails() {
        List<RewardDistributionStatus> savedStatuses = captureSavedStatuses();
        RewardSettlementResult settlement = settlement();
        RewardTransactionException submissionFailure = failure("submission failed");
        when(settlementService.settle(SETTLEMENT_DATE)).thenReturn(settlement);
        when(repository.findByBatchId(BATCH_ID)).thenReturn(Optional.empty());
        when(transactionService.submit(settlement)).thenThrow(submissionFailure);

        assertThatThrownBy(() -> service.distribute(SETTLEMENT_DATE))
                .isSameAs(submissionFailure);
        assertThat(savedStatuses).containsExactly(
                RewardDistributionStatus.PENDING,
                RewardDistributionStatus.FAILED
        );
    }

    @Test
    void keepsSubmittingStateWhenSubmissionResultIsUnknown() {
        List<RewardDistributionStatus> savedStatuses = captureSavedStatuses();
        RewardSettlementResult settlement = settlement();
        RewardSubmissionUnknownException submissionFailure = new RewardSubmissionUnknownException(
                new IOException("submission response lost")
        );
        when(settlementService.settle(SETTLEMENT_DATE)).thenReturn(settlement);
        when(repository.findByBatchId(BATCH_ID)).thenReturn(Optional.empty());
        when(transactionService.submit(settlement)).thenThrow(submissionFailure);

        assertThatThrownBy(() -> service.distribute(SETTLEMENT_DATE))
                .isSameAs(submissionFailure);
        assertThat(savedStatuses).containsExactly(RewardDistributionStatus.PENDING);
        verify(repository).claimForSubmission(any(), anyLong(), anyInt(), any(), any());
    }

    @Test
    void marksDistributionFailedWhenReceiptIsReverted() {
        List<RewardDistributionStatus> savedStatuses = captureSavedStatuses();
        RewardSettlementResult settlement = settlement();
        when(settlementService.settle(SETTLEMENT_DATE)).thenReturn(settlement);
        when(repository.findByBatchId(BATCH_ID)).thenReturn(Optional.empty());
        when(transactionService.submit(settlement)).thenReturn(Optional.of(transactionDTO()));
        when(transactionService.waitForConfirmation(TRANSACTION_HASH)).thenReturn(false);

        assertThatThrownBy(() -> service.distribute(SETTLEMENT_DATE))
                .isInstanceOf(RewardTransactionException.class)
                .hasRootCauseMessage("Reward transaction reverted: " + TRANSACTION_HASH);
        assertThat(savedStatuses).containsExactly(
                RewardDistributionStatus.PENDING,
                RewardDistributionStatus.SUBMITTED,
                RewardDistributionStatus.FAILED
        );
    }

    @Test
    void resumesSubmittedDistributionWithoutSubmittingAgain() {
        RewardDistribution distribution = pendingDistribution();
        distribution.markSubmitting(5L, 1);
        distribution.markSubmitted(TRANSACTION_HASH);
        when(repository.findByBatchId(BATCH_ID)).thenReturn(Optional.of(distribution));
        when(transactionService.waitForConfirmation(TRANSACTION_HASH)).thenReturn(true);

        Optional<RewardTransactionDTO> result = service.distribute(SETTLEMENT_DATE);

        assertThat(result).contains(transactionDTO());
        assertThat(distribution.getStatus()).isEqualTo(RewardDistributionStatus.CONFIRMED);
        verify(transactionService, never()).submit(any());
        verifyNoInteractions(settlementService);
    }

    @Test
    void returnsConfirmedDistributionWithoutSubmittingAgain() {
        RewardDistribution distribution = pendingDistribution();
        distribution.markSubmitting(5L, 1);
        distribution.markSubmitted(TRANSACTION_HASH);
        distribution.markConfirmed();
        when(repository.findByBatchId(BATCH_ID)).thenReturn(Optional.of(distribution));

        Optional<RewardTransactionDTO> result = service.distribute(SETTLEMENT_DATE);

        assertThat(result).contains(transactionDTO());
        verify(transactionService, never()).submit(any());
        verify(transactionService, never()).waitForConfirmation(any());
        verifyNoInteractions(settlementService);
    }

    @Test
    void recoversSubmittedHashFromOnChainEvent() {
        List<RewardDistributionStatus> savedStatuses = captureSavedStatuses();
        RewardDistribution distribution = pendingDistribution();
        distribution.markSubmitting(5L, 1);
        when(repository.findByBatchId(BATCH_ID)).thenReturn(Optional.of(distribution));
        when(transactionService.findTransactionHashByBatchId(BATCH_ID))
                .thenReturn(Optional.of(TRANSACTION_HASH));
        when(transactionService.waitForConfirmation(TRANSACTION_HASH)).thenReturn(true);

        Optional<RewardTransactionDTO> result = service.distribute(SETTLEMENT_DATE);

        assertThat(result).contains(transactionDTO());
        assertThat(savedStatuses).containsExactly(
                RewardDistributionStatus.SUBMITTED,
                RewardDistributionStatus.CONFIRMED
        );
        verify(transactionService, never()).submit(any());
        verifyNoInteractions(settlementService);
    }

    @Test
    void doesNotSubmitWhenAnotherRequestOwnsSubmission() {
        RewardDistribution pending = pendingDistribution();
        RewardDistribution submitting = pendingDistribution();
        submitting.markSubmitting(5L, 1);
        when(repository.findByBatchId(BATCH_ID))
                .thenReturn(Optional.of(pending), Optional.of(submitting));
        when(settlementService.settle(SETTLEMENT_DATE)).thenReturn(settlement());
        when(repository.claimForSubmission(any(), anyLong(), anyInt(), any(), any()))
                .thenReturn(0);
        when(transactionService.findTransactionHashByBatchId(BATCH_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.distribute(SETTLEMENT_DATE))
                .isInstanceOf(RewardTransactionException.class)
                .hasRootCauseMessage("Reward transaction submission is unresolved: " + BATCH_ID);
        verify(transactionService, never()).submit(any());
    }

    @Test
    void skipsPersistenceWhenSettlementIsEmpty() {
        RewardSettlementResult empty = new RewardSettlementResult(
                SETTLEMENT_DATE,
                List.of(),
                0L
        );
        when(settlementService.settle(SETTLEMENT_DATE)).thenReturn(empty);
        when(repository.findByBatchId(BATCH_ID)).thenReturn(Optional.empty());

        Optional<RewardTransactionDTO> result = service.distribute(SETTLEMENT_DATE);

        assertThat(result).isEmpty();
        verify(repository).findByBatchId(BATCH_ID);
    }

    private List<RewardDistributionStatus> captureSavedStatuses() {
        List<RewardDistributionStatus> statuses = new ArrayList<>();
        when(repository.saveAndFlush(any(RewardDistribution.class)))
                .thenAnswer(invocation -> {
                    RewardDistribution distribution = invocation.getArgument(0);
                    assignId(distribution);
                    statuses.add(distribution.getStatus());
                    return distribution;
                });
        return statuses;
    }

    private RewardSettlementResult settlement() {
        return new RewardSettlementResult(
                SETTLEMENT_DATE,
                List.of(new RewardRecipient(WALLET_ADDRESS, 5L)),
                5L
        );
    }

    private RewardDistribution pendingDistribution() {
        return assignId(RewardDistribution.pending(SETTLEMENT_DATE, BATCH_ID, 5L, 1));
    }

    private RewardDistribution assignId(RewardDistribution distribution) {
        if (distribution.getId() == null) {
            ReflectionTestUtils.setField(distribution, "id", 1L);
        }
        return distribution;
    }

    private RewardTransactionDTO transactionDTO() {
        return new RewardTransactionDTO(BATCH_ID, REWARD_DAY, TRANSACTION_HASH);
    }

    private RewardTransactionException failure(String message) {
        return new RewardTransactionException(new IllegalStateException(message));
    }
}
