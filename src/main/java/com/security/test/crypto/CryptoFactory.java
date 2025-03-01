package com.security.test.crypto;

public class CryptoFactory {
    public static AESCrypto getCrypto(AESMode mode) {
        return switch (mode) {
            case GCM -> new AES256GCMUtil(CryptoConfig.getInstance().getSecretKey(CryptoConstants.AES_GCM_ENCRYPTION_KEY));
            case CBC -> throw new UnsupportedOperationException("AES CBC mode is not supported yet");
            default -> throw new IllegalArgumentException("Invalid AES mode");
        };
    }
}
