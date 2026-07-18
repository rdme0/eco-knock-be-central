package jnu.econovation.ecoknockbecentral.wallet.event;

import jnu.econovation.ecoknockbecentral.member.event.MemberCreatedEvent;
import jnu.econovation.ecoknockbecentral.wallet.service.MemberWalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class MemberCreatedWalletEventListener {

    private final MemberWalletService memberWalletService;

    public MemberCreatedWalletEventListener(MemberWalletService memberWalletService) {
        this.memberWalletService = memberWalletService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createManagedWallet(MemberCreatedEvent event) {
        try {
            memberWalletService.createManagedWalletIfAbsent(event.getMemberId());
        } catch (Exception exception) {
            log.error("Failed to create managed wallet for memberId={}", event.getMemberId(), exception);
        }
    }
}
