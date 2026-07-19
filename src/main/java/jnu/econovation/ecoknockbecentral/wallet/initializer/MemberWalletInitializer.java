package jnu.econovation.ecoknockbecentral.wallet.initializer;

import jnu.econovation.ecoknockbecentral.wallet.service.MemberWalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MemberWalletInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemberWalletInitializer.class);

    private final MemberWalletService memberWalletService;

    public MemberWalletInitializer(MemberWalletService memberWalletService) {
        this.memberWalletService = memberWalletService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        int createdCount = memberWalletService.createManagedWalletsForExistingMembers();
        if (createdCount > 0) {
            LOGGER.info("Created managed wallets for {} existing members", createdCount);
        }
    }
}
