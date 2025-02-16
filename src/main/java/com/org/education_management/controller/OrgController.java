package com.org.education_management.controller;

import com.org.education_management.service.OrgService;
import com.org.education_management.util.OrgUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.org.education_management.module.fees.controller.ResponseEntityWrapper.buildResponse;

@RestController
@RequestMapping("api/org")
public class OrgController {

    private static final Logger logger = Logger.getLogger(OrgController.class.getName());

    OrgService orgService = new OrgService();

    @GetMapping(value = "/getOrgDetails", produces = "application/json")
    private ResponseEntity<Map<String, Object>> getOrgDetails(@RequestParam Map<String, Object> requestMap) {
        Map<String, Object> resultMap = new HashMap<>();
        Long orgID = requestMap.containsKey("orgID") ? (Long) requestMap.getOrDefault("orgID", 0L) : null;
        try {
            Map<Long, Object> orgDetailsMap = (Map<Long, Object>) orgService.getDetailsByOrgID(orgID);
            return buildResponse(HttpStatus.OK, "Organization details fetched successfully", orgDetailsMap);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when getting orgDetails : {0}", e);
            return buildResponse(HttpStatus.NOT_FOUND, "Request could,t be process, check logs!");
        }
    }

    @PostMapping("/createOrg")
    public ResponseEntity<Map<String, Object>> createOrg(@RequestBody Map<String, Object> requestMap) {
        Map<String, Object> resultMap = new HashMap<>();
        String orgName = (String) requestMap.get("orgName");
        String userEmail = (String) requestMap.get("userEmail");
        String password = (String) requestMap.get("password");
        String userName = (String) requestMap.get("userName");

        if(OrgUtil.getInstance().isEmailExists(userEmail)) {
            return buildResponse(HttpStatus.CONFLICT, "User email already exists! try login");
        }

        if(!orgService.validateOrgName(orgName)) {
            return buildResponse(HttpStatus.CONFLICT, "Organization name already found!...");
        }

        try {
            orgService.createOrg(orgName, userEmail, password, userName);
            return buildResponse(HttpStatus.OK, "Organization created successfully");
        } catch (Exception e) {
            return buildResponse(HttpStatus.BAD_REQUEST, "Unable to create organization! contact support");
        }
    }
}
