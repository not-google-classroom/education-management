package com.org.education_management.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MaskUtil {

    public static MaskUtil maskUtil = null;
    private static final Logger logger = Logger.getLogger(MaskUtil.class.getName());

    public static MaskUtil getInstance() {
        if(maskUtil == null) {
            maskUtil = new MaskUtil();
        }
        return maskUtil;
    }

    public String maskEmail(String userEmail) {
        if(userEmail != null && !userEmail.isEmpty()) {
            return userEmail.replaceAll("(?<=.{3}).(?=[^@]*?.@)", "*");
        }
        logger.log(Level.WARNING, "Provided email is null, unable to mask data");
        return null;
    }
}
