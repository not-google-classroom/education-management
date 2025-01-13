package com.org.education_management.controller;

import com.org.education_management.service.RolesService;
import com.org.education_management.service.UserService;
import com.org.education_management.util.StatusConstants;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("api/users")
public class UserController {

    UserService userService = new UserService();

    @GetMapping(value = "/getUsers", produces = "application/json")
    private Map<String, Object> getUsers(@RequestParam Map<String, Object> requestMap) {
        Long userID = null;
        if(requestMap != null && requestMap.containsKey("userID")) {
            userID = Long.parseLong((String) requestMap.getOrDefault("userID", null));
        }
        return userService.getUsers(userID);
    }

    @PostMapping(value = "/addUser", produces = "application/json")
    private Map<String, Object> addUser(@RequestBody  Map<String, Object> requestMap) {
        Map<String, Object> resultMap = new HashMap<>();
        if(requestMap != null && !requestMap.isEmpty()) {
            resultMap = userService.addUser(requestMap);
            return resultMap;
        }
        resultMap.put(StatusConstants.STATUS_CODE, 500);
        resultMap.put(StatusConstants.MESSAGE, "Internal server error when processing request!");
        return resultMap;
    }
}
