package com.security.crypto;


import com.org.education_management.util.FileHandler;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.Map;

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
            Map<String, String> props = new HashMap<>();
            props.put(CryptoConstants.AES_GCM_ENCRYPTION_KEY, AESKeyEncryptionUtil.getInstance().encryptAESKey(key));
            FileHandler.writePropsFile(CryptoConstants.APP_PROPERTIES_FILE, props);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate secret key", e);
        }
        return key;
    }
}