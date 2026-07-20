package jnu.econovation.ecoknockbecentral.wallet.exception;

import jnu.econovation.ecoknockbecentral.common.exception.client.ClientException;
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;

public class WalletNotFoundException extends ClientException {

    public WalletNotFoundException() {
        super(ErrorCode.WALLET_NOT_FOUND);
    }
}
