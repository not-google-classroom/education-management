package com.security.test.crypto;

public interface Crypto {
    String encrypt(String plainText) throws Exception;
    String decrypt(String encryptedText) throws Exception;
}
