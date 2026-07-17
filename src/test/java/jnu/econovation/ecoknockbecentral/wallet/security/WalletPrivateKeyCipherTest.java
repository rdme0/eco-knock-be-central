package jnu.econovation.ecoknockbecentral.wallet.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.SecureRandom;
import java.util.Base64;
import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException;
import org.junit.jupiter.api.Test;

class WalletPrivateKeyCipherTest {

    @Test
    void encryptsAndDecryptsPrivateKey() {
        WalletPrivateKeyCipher cipher = new WalletPrivateKeyCipher(randomEncryptionKey());
        String privateKey = "0x0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

        String encrypted = cipher.encrypt(privateKey);

        assertThat(encrypted).isNotEqualTo(privateKey);
        assertThat(cipher.decrypt(encrypted)).isEqualTo(privateKey);
    }

    @Test
    void usesRandomIvForEachEncryption() {
        WalletPrivateKeyCipher cipher = new WalletPrivateKeyCipher(randomEncryptionKey());
        String privateKey = "0x0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

        String first = cipher.encrypt(privateKey);
        String second = cipher.encrypt(privateKey);

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void rejectsEncryptionKeyWithInvalidLength() {
        String shortKey = Base64.getEncoder().encodeToString(new byte[16]);

        assertThatThrownBy(() -> new WalletPrivateKeyCipher(shortKey))
                .isInstanceOf(InternalServerException.class);
    }

    private String randomEncryptionKey() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}
