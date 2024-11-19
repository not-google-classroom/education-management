package com.org.education_management.util;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class PasswordUtil {

    public static String hashPassword(String plainTextPassword) {
        String salt = BCrypt.gensalt(12);
        return BCrypt.hashpw(plainTextPassword, salt);
    }

    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}
