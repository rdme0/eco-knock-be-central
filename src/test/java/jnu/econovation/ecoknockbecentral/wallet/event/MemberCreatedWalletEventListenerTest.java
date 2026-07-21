package jnu.econovation.ecoknockbecentral.wallet.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import jnu.econovation.ecoknockbecentral.member.event.MemberCreatedEvent;
import jnu.econovation.ecoknockbecentral.member.model.vo.Role;
import jnu.econovation.ecoknockbecentral.wallet.service.MemberWalletService;
import org.junit.jupiter.api.Test;

class MemberCreatedWalletEventListenerTest {

    @Test
    void doesNotCreateManagedWalletForGuest() {
        MemberWalletService memberWalletService = mock(MemberWalletService.class);

        MemberCreatedWalletEventListener listener = new MemberCreatedWalletEventListener(memberWalletService);

        listener.createManagedWallet(new MemberCreatedEvent(1L, Role.GUEST));

        verifyNoInteractions(memberWalletService);
    }

    @Test
    void createsManagedWalletForNonGuest() {
        MemberWalletService memberWalletService = mock(MemberWalletService.class);

        MemberCreatedWalletEventListener listener = new MemberCreatedWalletEventListener(memberWalletService);

        listener.createManagedWallet(new MemberCreatedEvent(2L, Role.USER));

        verify(memberWalletService).createManagedWalletIfAbsent(2L);
    }
}
