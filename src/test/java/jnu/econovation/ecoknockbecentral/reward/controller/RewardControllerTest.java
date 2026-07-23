package jnu.econovation.ecoknockbecentral.reward.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse;
import jnu.econovation.ecoknockbecentral.common.security.dto.EcoKnockUserDetails;
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO;
import jnu.econovation.ecoknockbecentral.member.model.vo.Role;
import jnu.econovation.ecoknockbecentral.reward.dto.request.RewardHistoryRequest;
import jnu.econovation.ecoknockbecentral.reward.dto.response.RewardHistoryResponse;
import jnu.econovation.ecoknockbecentral.reward.model.vo.RewardType;
import jnu.econovation.ecoknockbecentral.reward.service.RewardHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class RewardControllerTest {

    @Test
    void delegatesAuthenticatedMemberIdAndWrapsPage() {
        RewardHistoryService service = mock(RewardHistoryService.class);
        RewardController controller = new RewardController(service);
        EcoKnockUserDetails userDetails = new EcoKnockUserDetails(new MemberInfoDTO(
                7L, 700L, Role.USER, null, "reward-test-member", null, null
        ));
        Page<RewardHistoryResponse> expected = new PageImpl<>(List.of(new RewardHistoryResponse(
                RewardType.ATTENDANCE, 5L, null, LocalDate.of(2026, 7, 21)
        )));
        when(service.getMyHistory(7L, new RewardHistoryRequest(1, 5))).thenReturn(expected);

        ResponseEntity<CommonResponse<Page<RewardHistoryResponse>>> response =
                controller.getMyRewardHistory(1, 5, userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().result()).isEqualTo(expected);
        verify(service).getMyHistory(7L, new RewardHistoryRequest(1, 5));
    }
}
