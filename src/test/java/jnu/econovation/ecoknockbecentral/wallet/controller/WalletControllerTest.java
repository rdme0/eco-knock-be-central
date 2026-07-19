package jnu.econovation.ecoknockbecentral.wallet.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse;
import jnu.econovation.ecoknockbecentral.common.security.dto.EcoKnockUserDetails;
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO;
import jnu.econovation.ecoknockbecentral.member.model.vo.Role;
import jnu.econovation.ecoknockbecentral.wallet.dto.response.WalletBalanceResponse;
import jnu.econovation.ecoknockbecentral.wallet.model.vo.WalletType;
import jnu.econovation.ecoknockbecentral.wallet.service.WalletBalanceService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class WalletControllerTest {

    @Test
    void delegatesAuthenticatedMemberIdAndWrapsResponse() {
        WalletBalanceService service = mock(WalletBalanceService.class);
        WalletController controller = new WalletController(service);
        MemberInfoDTO memberInfo = new MemberInfoDTO(
                7L,
                700L,
                Role.USER,
                null,
                "wallet-test-member",
                null,
                null
        );
        EcoKnockUserDetails userDetails = new EcoKnockUserDetails(memberInfo);
        WalletBalanceResponse expected = new WalletBalanceResponse(
                "0x6813d7a3e620f0c536876b41bfbb56779ea6e7e8",
                WalletType.MANAGED,
                "13.5",
                "KRT"
        );
        when(service.getWalletBalance(7L)).thenReturn(expected);

        ResponseEntity<CommonResponse<WalletBalanceResponse>> response =
                controller.getMyWalletBalance(userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().result()).isEqualTo(expected);
        verify(service).getWalletBalance(7L);
    }
}
