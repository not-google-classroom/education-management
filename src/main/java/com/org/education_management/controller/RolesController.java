package com.org.education_management.controller;

import com.org.education_management.service.RolesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.org.education_management.module.fees.controller.ResponseEntityWrapper.buildResponse;

@RestController
@RequestMapping("api/roles")
public class RolesController {

    private static final Logger logger = Logger.getLogger(RolesController.class.getName());;

    @GetMapping(value = "/getAllRoles", produces = "application/json")
    private ResponseEntity<Map<String, Object>> getRoles(@RequestParam Map<String, Object> requestMap) {
        Long roleID = null;
        try {
            if (requestMap != null && requestMap.containsKey("roleID")) {
                roleID = Long.parseLong((String) requestMap.getOrDefault("roleID", null));
            }
            Map<String, Object> result = RolesService.getInstance().getAllRoles(roleID);
            return buildResponse(HttpStatus.OK, "user roles fetched successfully", result);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when fetching user roles : {0}", e);
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Request cannot be processed, contact support");
        }
    }
}
