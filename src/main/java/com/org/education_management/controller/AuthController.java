package com.org.education_management.controller;

import com.org.education_management.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.org.education_management.module.fees.controller.ResponseEntityWrapper.buildResponse;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private static final Logger logger = Logger.getLogger(AuthController.class.getName());
    AuthService authService = new AuthService();

    @PostMapping("login")
    private ResponseEntity<Map<String, Object>> loginToService(@RequestBody Map<String, Object> requestMap, HttpServletRequest request, HttpServletResponse response) {
        if (requestMap.containsKey("userEmail") && requestMap.containsKey("password")) {
            logger.log(Level.INFO, "login for user initiated...");
            try {
                String token = authService.validateLogin(requestMap);
                if (token != null) {
                    response.addCookie(new Cookie("token", token));
                    return buildResponse(HttpStatus.OK, "user logged in successfully", new JSONObject().put("token", token));
                } else {
                    return buildResponse(HttpStatus.NO_CONTENT, "user details not found! try login");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception when logging in user ", e);
            }
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error when processing your request,contact support");
    }
}
