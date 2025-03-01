package com.security.test.crypto;


import com.org.education_management.util.FileHandler;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Properties;

public abstract class AESCrypto implements Crypto{
    protected static final String AES_ALGORITHM = "AES";
    protected static final int AES_KEY_SIZE = 256; // AES-256

    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGenerator.init(AES_KEY_SIZE);
        return keyGenerator.generateKey();
    }

    protected static SecretKey generateNewSecretKey() {
        SecretKey key = null;
        try {
            key = generateKey();
            Properties props = new Properties();
            props.setProperty(CryptoConstants.AES_GCM_ENCRYPTION_KEY, AESKeyEncryptionUtil.getInstance().encryptAESKey(key));
            FileHandler.writePropsFile(CryptoConstants.APP_PROPERTIES_FILE, props, Boolean.TRUE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate secret key", e);
        }
        return key;
    }
}