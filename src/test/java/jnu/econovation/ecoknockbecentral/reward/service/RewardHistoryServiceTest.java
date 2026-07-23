package jnu.econovation.ecoknockbecentral.reward.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import jnu.econovation.ecoknockbecentral.reward.dto.request.RewardHistoryRequest;
import jnu.econovation.ecoknockbecentral.reward.dto.response.RewardHistoryResponse;
import jnu.econovation.ecoknockbecentral.reward.model.entity.RewardDistribution;
import jnu.econovation.ecoknockbecentral.reward.model.entity.RewardHistory;
import jnu.econovation.ecoknockbecentral.reward.model.vo.RewardType;
import jnu.econovation.ecoknockbecentral.reward.repository.RewardHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class RewardHistoryServiceTest {

    @Test
    void delegatesMemberPageabilityAndMapsConfirmedHistory() {
        RewardHistoryRepository repository = mock(RewardHistoryRepository.class);
        RewardHistoryService service = new RewardHistoryService(repository);
        RewardHistory history = mock(RewardHistory.class);
        RewardDistribution distribution = mock(RewardDistribution.class);
        when(history.getRewardType()).thenReturn(RewardType.STAY_DURATION);
        when(history.getRewardAmount()).thenReturn(2L);
        when(history.getStayHours()).thenReturn(2);
        when(history.getRewardDistribution()).thenReturn(distribution);
        when(distribution.getSettlementDate()).thenReturn(LocalDate.of(2026, 7, 21));
        when(repository.findConfirmedByMemberId(eq(7L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(
                        List.of(history),
                        PageRequest.of(2, 10),
                        21
                ));

        var result = service.getMyHistory(7L, new RewardHistoryRequest(2, 10));

        assertThat(result.getContent()).containsExactly(new RewardHistoryResponse(
                RewardType.STAY_DURATION,
                2L,
                2,
                LocalDate.of(2026, 7, 21)
        ));
        assertThat(result.getPageable().getPageNumber()).isEqualTo(2);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        verify(repository).findConfirmedByMemberId(eq(7L), any(Pageable.class));
    }
}
