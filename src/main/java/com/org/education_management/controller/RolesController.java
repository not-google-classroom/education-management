package com.org.education_management.controller;

import com.org.education_management.service.RolesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("api/roles")
public class RolesController {

    private static final Logger logger = Logger.getLogger(RolesController.class.getName());;

    @GetMapping(value = "/getAllRoles", produces = "application/json")
    private Map<String, Object> getRoles(@RequestParam Map<String, Object> requestMap) {
        Long roleID = null;
        if(requestMap != null && requestMap.containsKey("roleID")) {
            roleID = Long.parseLong((String) requestMap.getOrDefault("roleID", null));
        }
        return RolesService.getInstance().getAllRoles(roleID);
    }
}
