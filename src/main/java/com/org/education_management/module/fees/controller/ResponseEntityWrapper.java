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
        if(body != null && !body.isEmpty()) {
            response.put(StatusConstants.DATA, body.toMap());
        }
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

    public static ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message, Map mapData) {
        Map<String, Object> response = new HashMap<>();
        response.put(StatusConstants.STATUS_CODE, status.value());
        response.put(StatusConstants.MESSAGE, message);
        if(mapData.containsKey(StatusConstants.STATUS_CODE)) {
            response.put(StatusConstants.DATA, mapData.get(StatusConstants.DATA));
        } else {
            response.put(StatusConstants.DATA, mapData);
        }
        return ResponseEntity.status(status).body(response);
    }

    public static ResponseEntity<Map<String, Object>> buildResponse(Map responseData) {
        if(responseData.containsKey(StatusConstants.STATUS_CODE)) {
            HttpStatus status = (HttpStatus) responseData.get(StatusConstants.STATUS_CODE);
            if(responseData.containsKey(StatusConstants.MESSAGE) || responseData.containsKey(StatusConstants.DATA)) {
                return ResponseEntity.status(status).body(responseData);
            }
            return buildResponse(status);
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid request received from entity");
    }
}
