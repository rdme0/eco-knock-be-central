package jnu.econovation.ecoknockbecentral.wallet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Optional;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import jnu.econovation.ecoknockbecentral.wallet.client.KrtBalanceClient;
import jnu.econovation.ecoknockbecentral.wallet.dto.response.WalletBalanceResponse;
import jnu.econovation.ecoknockbecentral.wallet.exception.WalletNotFoundException;
import jnu.econovation.ecoknockbecentral.wallet.model.entity.MemberWallet;
import jnu.econovation.ecoknockbecentral.wallet.model.vo.WalletType;
import jnu.econovation.ecoknockbecentral.wallet.repository.MemberWalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WalletBalanceServiceTest {

    private static final String WALLET_ADDRESS = "0x6813d7a3e620f0c536876b41bfbb56779ea6e7e8";

    private MemberWalletRepository memberWalletRepository;
    private KrtBalanceClient krtBalanceClient;
    private WalletBalanceService service;

    @BeforeEach
    void setUp() {
        memberWalletRepository = mock(MemberWalletRepository.class);
        krtBalanceClient = mock(KrtBalanceClient.class);
        service = new WalletBalanceService(memberWalletRepository, krtBalanceClient);
    }

    @Test
    void returnsActiveExternalWalletAndKrtBalance() {
        MemberWallet wallet = mock(MemberWallet.class);
        when(wallet.getWalletAddress()).thenReturn(WALLET_ADDRESS);
        when(wallet.getWalletType()).thenReturn(WalletType.EXTERNAL);
        when(memberWalletRepository.findByMemberIdAndActiveRewardDestinationTrue(1L))
                .thenReturn(Optional.of(wallet));
        when(krtBalanceClient.getBalance(WALLET_ADDRESS))
                .thenReturn(new BigInteger("13000000000000000000"));

        WalletBalanceResponse response = service.getWalletBalance(1L);

        assertThat(response.walletAddress()).isEqualTo(WALLET_ADDRESS);
        assertThat(response.walletType()).isEqualTo(WalletType.EXTERNAL);
        assertThat(response.balance()).isEqualTo("13");
        assertThat(response.symbol()).isEqualTo("KRT");
        verify(memberWalletRepository).findByMemberIdAndActiveRewardDestinationTrue(1L);
    }

    @Test
    void formatsEighteenDecimalBalanceWithoutPrecisionLoss() {
        when(memberWalletRepository.findByMemberIdAndActiveRewardDestinationTrue(2L))
                .thenReturn(Optional.of(managedWallet()));
        when(krtBalanceClient.getBalance(WALLET_ADDRESS))
                .thenReturn(new BigInteger("13500000000000000001"));

        WalletBalanceResponse response = service.getWalletBalance(2L);

        assertThat(response.balance()).isEqualTo("13.500000000000000001");
    }

    @Test
    void throwsWhenActiveRewardWalletDoesNotExist() {
        when(memberWalletRepository.findByMemberIdAndActiveRewardDestinationTrue(3L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getWalletBalance(3L))
                .isInstanceOf(WalletNotFoundException.class);
        verifyNoInteractions(krtBalanceClient);
    }

    private MemberWallet managedWallet() {
        return MemberWallet.managed(
                mock(Member.class),
                WALLET_ADDRESS,
                "encrypted-private-key",
                true
        );
    }
}
