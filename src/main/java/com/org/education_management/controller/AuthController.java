package com.org.education_management.controller;

import com.org.education_management.service.AuthService;
import com.org.education_management.util.StatusConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private static final Logger logger = Logger.getLogger(AuthController.class.getName());
    AuthService authService = new AuthService();

    @PostMapping("login")
    private Map<String, Object> loginToService(@RequestBody Map<String, Object> requestMap, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        if (requestMap.containsKey("userEmail") && requestMap.containsKey("password")) {
            logger.log(Level.INFO, "login for user initiated...");
            try {
                String token = authService.validateLogin(requestMap);
                if (token != null) {
                    result.put(StatusConstants.STATUS_CODE, 200);
                    result.put(StatusConstants.MESSAGE, "User logged in successfully");
                    result.put("token", token);
                    response.addCookie(new Cookie("token", token));
                    return result;
                } else {
                    result.put(StatusConstants.STATUS_CODE, 204);
                    result.put(StatusConstants.MESSAGE, "User details not found! try creating account to continue.");
                    return result;
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception when logging in user ", e);
            }
        }
        result.put(StatusConstants.STATUS_CODE, 500);
        result.put(StatusConstants.MESSAGE, "Internal error occurred when processing request");
        return result;
    }
}
