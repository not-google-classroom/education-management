package com.org.education_management.service;

import com.org.education_management.util.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthService {
    private static final Logger logger = Logger.getLogger(AuthService.class.getName());

    public String validateLogin(Map<String, Object> requestMap) throws Exception {
        try {
            String userEmail = (String) requestMap.get("userEmail");
            String password = (String) requestMap.get("password");
            Long userID = OrgUtil.getInstance().getUserIDByEmail(userEmail);
            if(userID != null) {
                String schemaName = OrgUtil.getInstance().getSchemaName(userID);
                if(schemaName != null) {
                    SchemaUtil.getInstance().setSearchPathForSchema(schemaName);
                    if(UserMgmtUtil.getInstance().validateUser(userEmail, password)) {
                        String subject = userEmail + "," + schemaName;
                        subject = Base64.getEncoder().encodeToString(subject.getBytes(StandardCharsets.UTF_8));
                        return JWTUtil.generateToken(subject);
                    }
                }
            } else {
                logger.log(Level.WARNING, "user details not found for email : {0}", MaskUtil.getInstance().maskEmail(userEmail));
            }
        } finally {
            SchemaUtil.getInstance().setSearchPathToPublic();
        }
        return null;
    }
}
