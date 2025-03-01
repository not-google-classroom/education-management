package com.security.test.crypto;

import com.org.education_management.util.FileHandler;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;

public class MasterKeyUtil {

    private static SecretKey createMasterKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(AESCrypto.AES_ALGORITHM);
        keyGen.init(AESCrypto.AES_KEY_SIZE, new SecureRandom()); // AES-256
        return keyGen.generateKey();
    }

    public static String encodeKey(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static SecretKey decodeKey(String key) {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, AESCrypto.AES_ALGORITHM);
    }

    public static void generateMasterKey() throws Exception{
        Properties props = new Properties();
        props.setProperty(CryptoConstants.AES_MASTER_KEY, encodeKey(createMasterKey()));
        FileHandler.writePropsFile(CryptoConstants.APP_PROPERTIES_FILE, props, Boolean.TRUE);
    }

    public static SecretKey getMasterKey() {
        try {
            return decodeKey(FileHandler.readPropsFile(CryptoConstants.APP_PROPERTIES_FILE).get(CryptoConstants.AES_MASTER_KEY));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load master key", e);
        }
    }

}
