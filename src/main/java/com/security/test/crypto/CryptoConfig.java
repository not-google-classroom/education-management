package com.security.test.crypto;

import com.org.education_management.util.FileHandler;
import java.util.HashMap;
import javax.crypto.SecretKey;

// Need to enhance this class.
public class CryptoConfig {
    private static final String PROPERTIES_FILE = CryptoConstants.APP_PROPERTIES_FILE;
    private static CryptoConfig instance;
    private static HashMap<String, SecretKey> secretKey;

    private CryptoConfig() {
        HashMap <String, SecretKey> secretKey = new HashMap<>();
        try {
            HashMap <String, String> props = FileHandler.readPropsFile(PROPERTIES_FILE);
            if(props.containsKey(CryptoConstants.AES_GCM_ENCRYPTION_KEY)){
                secretKey.put(CryptoConstants.AES_GCM_ENCRYPTION_KEY, AESKeyEncryptionUtil.getInstance().decryptAESKey(props.get(CryptoConstants.AES_GCM_ENCRYPTION_KEY)));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load encryption key from properties", e);
        }
        CryptoConfig.secretKey = secretKey;
    }

    public static synchronized CryptoConfig getInstance() {
        if (instance == null) {
            instance = new CryptoConfig();
        }
        return instance;
    }

    public SecretKey getSecretKey(String keyName) {
        return  secretKey.containsKey(keyName)? secretKey.get(keyName):generateSecretKey(keyName);
    }

    public SecretKey generateSecretKey(String keyName){
        if(keyName.equalsIgnoreCase(CryptoConstants.AES_GCM_ENCRYPTION_KEY)){
            return AESCrypto.generateNewSecretKey();
        }
        return null;
    }

}
