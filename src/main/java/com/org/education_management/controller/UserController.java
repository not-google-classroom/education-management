package com.org.education_management.controller;

import com.org.education_management.service.RolesService;
import com.org.education_management.service.UserService;
import com.org.education_management.util.StatusConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.org.education_management.module.fees.controller.ResponseEntityWrapper.buildResponse;

@RestController
@RequestMapping("api/users")
public class UserController {

    UserService userService = new UserService();

    @GetMapping(value = "/getUsers", produces = "application/json")
    private ResponseEntity<Map<String, Object>> getUsers(@RequestParam Map<String, Object> requestMap) {
        Long userID = null;
        try {
            if (requestMap != null && requestMap.containsKey("userID")) {
                userID = Long.parseLong((String) requestMap.getOrDefault("userID", null));
            }
            Map<String, Object> resultMap = userService.getUsers(userID);
            return buildResponse(HttpStatus.OK, "user details fetched successfully", resultMap);
        } catch (Exception e) {
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Request cannot be processed, contact support");
        }
    }

    @PostMapping(value = "/addUser", produces = "application/json")
    private ResponseEntity<Map<String, Object>> addUser(@RequestBody  Map<String, Object> requestMap) {
        Map<String, Object> resultMap = new HashMap<>();
        if(requestMap != null && !requestMap.isEmpty()) {
            resultMap = userService.addUser(requestMap);
            return buildResponse(resultMap);
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error when processing the request, contact support");
    }

    @GetMapping(value = "/getUserGroups", produces = "application/json")
    private ResponseEntity<Map<String, Object>> getUserGroups(@RequestParam Map<String, Object> requestMap) {
        Map<String, Object> resultMap;
        try {
            Long ugID = null;
            if (requestMap != null && requestMap.containsKey("ugID")) {
                ugID = Long.parseLong((String) requestMap.getOrDefault("ugID", null));
            }
            resultMap = userService.getUserGroups(ugID);
            if (resultMap != null && !resultMap.isEmpty() && resultMap.get(StatusConstants.STATUS_CODE).equals(200)) {
                return buildResponse(HttpStatus.OK, "users group data fetched successfully", resultMap);
            }
            return buildResponse(HttpStatus.NO_CONTENT, "No users group found!");
        } catch (Exception e) {
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Request couldn't be processed, contact support");
        }

    }

    @PostMapping(value = "/addUserGroup", produces = "application/json")
    private ResponseEntity<Map<String, Object>> addUserGroup(@RequestBody Map<String, Object> requestMap) {
        try {
            if(requestMap != null && !requestMap.isEmpty()) {
                if (userService.addUsersGroup(requestMap)) {
                    return buildResponse(HttpStatus.OK, "users group created successfully");
                } else {
                    return buildResponse(HttpStatus.BAD_REQUEST, "unable to create user group");
                }
            }
            return buildResponse(HttpStatus.BAD_REQUEST, "Input data mismatch! verify data");
        } catch (Exception e) {
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Request couldn't be processed, contact support");
        }
    }
}
