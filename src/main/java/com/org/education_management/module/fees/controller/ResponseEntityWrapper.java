package com.org.education_management.module.fees.controller;

import com.org.education_management.util.StatusConstants;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ResponseEntityWrapper {
    public static ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message, JSONObject body) {
        Map<String, Object> response = new HashMap<>();
        response.put(StatusConstants.STATUS_CODE, status.value());
        response.put(StatusConstants.MESSAGE, message);
        response.put(StatusConstants.DATA, body);
        return ResponseEntity.status(status).body(response);
    }

    public static ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        return buildResponse(status, message, new JSONObject());
    }

    public static ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, JSONObject body) {
        return buildResponse(status, "", body);
    }

    public static ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status) {
        return buildResponse(status, "");
    }
}
