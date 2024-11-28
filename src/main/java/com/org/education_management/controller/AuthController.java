package com.org.education_management.controller;

import com.org.education_management.service.AuthService;
import com.org.education_management.util.StatusConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    private Map<String, Object> loginToService(@RequestParam Map<String, Object> requestMap, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        if(requestMap.containsKey("userEmail") && requestMap.containsKey("password")) {
            logger.log(Level.INFO, "login for user initiated...");
            String token = authService.validateLogin(requestMap);
            if(token != null) {
                result.put(StatusConstants.STATUS_CODE, 200);
                result.put(StatusConstants.MESSAGE, "user logged in successfully");
                response.addCookie(new Cookie("token", token));
                return result;
            }
        }
        result.put(StatusConstants.STATUS_CODE, 500);
        result.put(StatusConstants.MESSAGE, "Internal error occurred when processing request");
        return result;
    }
}
