package com.security.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AES256GCMUtil extends AESCrypto {

    private static final String AES_CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_SIZE = 12;
    private final SecretKey secretKey;

    public AES256GCMUtil(SecretKey secretKey){
        this.secretKey = secretKey;
    }

    @Override
    public String encrypt(String plaintext) throws Exception {
        System.out.println(secretKey);
        return encrypt(plaintext, secretKey, CryptoUtil.generateIV(IV_SIZE));
    }

    public String encrypt(String plaintext, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    @Override
    public String decrypt(String encryptedData) throws Exception{
        return decrypt(encryptedData, secretKey);
    }

    public String decrypt(String encryptedData, SecretKey key) throws Exception {
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);

        byte[] iv = new byte[IV_SIZE];
        byte[] cipherText = new byte[decodedData.length - IV_SIZE];
        System.arraycopy(decodedData, 0, iv, 0, IV_SIZE);
        System.arraycopy(decodedData, IV_SIZE, cipherText, 0, cipherText.length);

        Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        byte[] decryptedBytes = cipher.doFinal(cipherText);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

}
