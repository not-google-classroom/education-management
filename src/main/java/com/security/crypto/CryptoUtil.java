package com.security.crypto;

import java.security.SecureRandom;

public class CryptoUtil {
    private static Crypto cryptInstance = null;
    public static Crypto getInstance(Crypto instance) {
        if(cryptInstance == null){
            cryptInstance = instance;
        }
        return cryptInstance;
    }

    // Generate Secure Random IV
    public static byte[] generateIV(int ivSize) {
        byte[] iv = new byte[ivSize];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public static void main(String[] args) {
        try {
            MasterKeyUtil.generateMasterKey();
            Crypto aes256GCMUtil = CryptoFactory.getCrypto(AESMode.GCM);
            String plainText = "Hello World!";
            String encryptedData = aes256GCMUtil.encrypt(plainText);
            System.out.println("Encrypted Data: " + encryptedData);
            String decryptedData = aes256GCMUtil.decrypt(encryptedData);
            System.out.println("Decrypted Data: " + decryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
