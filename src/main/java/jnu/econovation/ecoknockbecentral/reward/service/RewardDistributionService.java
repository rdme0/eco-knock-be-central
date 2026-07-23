package jnu.econovation.ecoknockbecentral.reward.service;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import jnu.econovation.ecoknockbecentral.reward.dto.RewardSettlementResult;
import jnu.econovation.ecoknockbecentral.reward.dto.RewardTransactionDTO;
import jnu.econovation.ecoknockbecentral.reward.exception.RewardSubmissionUnknownException;
import jnu.econovation.ecoknockbecentral.reward.exception.RewardTransactionException;
import jnu.econovation.ecoknockbecentral.reward.model.entity.RewardDistribution;
import jnu.econovation.ecoknockbecentral.reward.model.vo.RewardDistributionStatus;
import jnu.econovation.ecoknockbecentral.reward.repository.RewardDistributionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RewardDistributionService {

    private static final List<RewardDistributionStatus> CLAIMABLE_STATUSES = List.of(
            RewardDistributionStatus.PENDING,
            RewardDistributionStatus.FAILED
    );

    private final RewardSettlementService rewardSettlementService;
    private final RewardTransactionService rewardTransactionService;
    private final RewardDistributionRepository rewardDistributionRepository;
    private final RewardHistoryService rewardHistoryService;

    public Optional<RewardTransactionDTO> distribute(LocalDate settlementDate) {
        String batchId = rewardTransactionService.createBatchId(settlementDate);
        Optional<RewardDistribution> existing = rewardDistributionRepository.findByBatchId(batchId);
        if (existing.isPresent() && !isClaimable(existing.get())) {
            return resumeInFlight(existing.get());
        }

        RewardSettlementResult settlement = rewardSettlementService.settle(settlementDate);
        if (settlement.recipients().isEmpty()) {
            return Optional.empty();
        }
        RewardDistribution distribution = existing.orElseGet(
                () -> createOrLoadPending(settlement)
        );
        rewardHistoryService.savePlannedHistories(distribution, settlement.recipients());
        return claimAndSubmit(distribution, settlement);
    }

    private RewardDistribution createOrLoadPending(RewardSettlementResult settlement) {
        String batchId = rewardTransactionService.createBatchId(settlement.settlementDate());
        RewardDistribution distribution = RewardDistribution.pending(
                settlement.settlementDate(),
                batchId,
                settlement.totalRewardAmount(),
                settlement.recipients().size()
        );
        try {
            return rewardDistributionRepository.saveAndFlush(distribution);
        } catch (DataIntegrityViolationException exception) {
            return rewardDistributionRepository.findByBatchId(batchId)
                    .orElseThrow(() -> exception);
        }
    }

    private Optional<RewardTransactionDTO> claimAndSubmit(
            RewardDistribution distribution,
            RewardSettlementResult settlement
    ) {
        if (!isClaimable(distribution)) {
            return resumeInFlight(distribution);
        }

        int claimed = rewardDistributionRepository.claimForSubmission(
                distribution.getId(),
                settlement.totalRewardAmount(),
                settlement.recipients().size(),
                RewardDistributionStatus.SUBMITTING,
                CLAIMABLE_STATUSES
        );
        if (claimed == 0) {
            RewardDistribution current = rewardDistributionRepository
                    .findByBatchId(distribution.getBatchId())
                    .orElseThrow(() -> failure("Reward distribution disappeared while claiming"));
            return resumeInFlight(current);
        }

        distribution.markSubmitting(
                settlement.totalRewardAmount(),
                settlement.recipients().size()
        );
        return submitAndConfirm(distribution, settlement);
    }

    private Optional<RewardTransactionDTO> submitAndConfirm(
            RewardDistribution distribution,
            RewardSettlementResult settlement
    ) {
        RewardTransactionDTO submitted;
        try {
            submitted = rewardTransactionService.submit(settlement)
                    .orElseThrow(() -> failure("Reward settlement unexpectedly became empty"));
        } catch (RewardSubmissionUnknownException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            distribution.markFailed();
            rewardDistributionRepository.saveAndFlush(distribution);
            throw exception;
        }

        distribution.markSubmitted(submitted.transactionHash());
        distribution = rewardDistributionRepository.saveAndFlush(distribution);
        return confirm(distribution, submitted);
    }

    private Optional<RewardTransactionDTO> resumeInFlight(
            RewardDistribution distribution
    ) {
        return switch (distribution.getStatus()) {
            case CONFIRMED -> Optional.of(toTransactionDTO(distribution));
            case SUBMITTED -> confirm(distribution, toTransactionDTO(distribution));
            case SUBMITTING -> recoverSubmitting(distribution);
            default -> throw failure(
                    "Reward distribution is not in an in-flight state: "
                            + distribution.getStatus()
            );
        };
    }

    private Optional<RewardTransactionDTO> recoverSubmitting(
            RewardDistribution distribution
    ) {
        Optional<String> recoveredHash = rewardTransactionService
                .findTransactionHashByBatchId(distribution.getBatchId());
        if (recoveredHash.isEmpty()) {
            throw failure(
                    "Reward transaction submission is unresolved: "
                            + distribution.getBatchId()
            );
        }

        distribution.markSubmitted(recoveredHash.get());
        distribution = rewardDistributionRepository.saveAndFlush(distribution);
        return confirm(distribution, toTransactionDTO(distribution));
    }

    private Optional<RewardTransactionDTO> confirm(
            RewardDistribution distribution,
            RewardTransactionDTO transaction
    ) {
        boolean confirmed = rewardTransactionService.waitForConfirmation(
                transaction.transactionHash()
        );
        if (!confirmed) {
            distribution.markFailed();
            rewardDistributionRepository.saveAndFlush(distribution);
            throw failure("Reward transaction reverted: " + transaction.transactionHash());
        }

        distribution.markConfirmed();
        rewardDistributionRepository.saveAndFlush(distribution);
        return Optional.of(transaction);
    }

    private RewardTransactionDTO toTransactionDTO(RewardDistribution distribution) {
        BigInteger rewardDay = new BigInteger(
                distribution.getSettlementDate().format(DateTimeFormatter.BASIC_ISO_DATE)
        );
        return new RewardTransactionDTO(
                distribution.getBatchId(),
                rewardDay,
                distribution.getTransactionHash()
        );
    }

    private boolean isClaimable(RewardDistribution distribution) {
        return CLAIMABLE_STATUSES.contains(distribution.getStatus());
    }

    private RewardTransactionException failure(String message) {
        return new RewardTransactionException(new IllegalStateException(message));
    }
}
