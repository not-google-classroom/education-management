package com.security.test.crypto;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESKeyEncryptionUtil {
    private static final String AES_ALGORITHM = "AES";
    private static final SecretKey masterKey = MasterKeyUtil.getMasterKey();

    private static AESKeyEncryptionUtil aesKeyEncryptionUtil = null;

    public static AESKeyEncryptionUtil getInstance() {
        if(aesKeyEncryptionUtil == null) {
            aesKeyEncryptionUtil = new AESKeyEncryptionUtil();
        }
        return aesKeyEncryptionUtil;
    }

    public String encryptAESKey(SecretKey aesKey) throws Exception {
        return encryptAESKey(aesKey, masterKey);
    }

    private String encryptAESKey(SecretKey aesKey, SecretKey masterKey) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, masterKey);
        byte[] encryptedKey = cipher.doFinal(aesKey.getEncoded());
        return Base64.getEncoder().encodeToString(encryptedKey);
    }

    public SecretKey decryptAESKey(String encryptedKeyBase64) throws Exception {
        return decryptAESKey(encryptedKeyBase64, masterKey);
    }

    private static SecretKey decryptAESKey(String encryptedKeyBase64, SecretKey masterKey) throws Exception {
        byte[] encryptedKey = Base64.getDecoder().decode(encryptedKeyBase64);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, masterKey);
        byte[] decryptedKey = cipher.doFinal(encryptedKey);
        return new SecretKeySpec(decryptedKey, AES_ALGORITHM);
    }
}
