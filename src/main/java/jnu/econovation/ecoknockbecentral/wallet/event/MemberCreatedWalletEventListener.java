package jnu.econovation.ecoknockbecentral.wallet.event;

import jnu.econovation.ecoknockbecentral.member.event.MemberCreatedEvent;
import jnu.econovation.ecoknockbecentral.member.model.vo.Role;
import jnu.econovation.ecoknockbecentral.wallet.service.MemberWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberCreatedWalletEventListener {

    private final MemberWalletService memberWalletService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createManagedWallet(MemberCreatedEvent event) {
        try {
            // 게스트 회원가입 시 지갑 생성 차단
            if (event.getRole() == Role.GUEST) {
                return;
            }

            memberWalletService.createManagedWalletIfAbsent(event.getMemberId());
        } catch (Exception exception) {
            log.error("Failed to create managed wallet for memberId={}", event.getMemberId(), exception);
        }
    }
}
