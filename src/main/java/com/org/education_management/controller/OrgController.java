package com.org.education_management.controller;

import com.org.education_management.service.OrgService;
import com.org.education_management.util.OrgUtil;
import com.org.education_management.util.StatusConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("api/org")
public class OrgController {

    private static final Logger logger = Logger.getLogger(OrgController.class.getName());

    OrgService orgService = new OrgService();

    @GetMapping("/csrf-token")
    public String generateToken(HttpServletRequest request, HttpServletResponse response) {
        return "token generated and stored successfully";
    }

    @GetMapping(value = "/getOrgDetails", produces = "application/json")
    private Map<String, Object> getOrgDetails(@RequestParam Map<String, Object> requestMap) {
        Map<String, Object> resultMap = new HashMap<>();
        Long orgID = requestMap.containsKey("orgID") ? (Long) requestMap.getOrDefault("orgID", 0L) : null;
        try {
            Map<Long, Object> orgDetailsMap = (Map<Long, Object>) orgService.getDetailsByOrgID(orgID);
            resultMap.put(StatusConstants.DATA, orgDetailsMap);
            resultMap.put(StatusConstants.MESSAGE, "Organization details fetched successfully");
            resultMap.put(StatusConstants.STATUS_CODE, 200);
            return resultMap;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when getting orgDetails : {0}", e);
            resultMap.put(StatusConstants.STATUS_CODE, 500);
            resultMap.put(StatusConstants.MESSAGE, "Internal server error!");
        }
        resultMap.put(StatusConstants.STATUS_CODE, 204);
        resultMap.put(StatusConstants.MESSAGE, "Unable to process the request!");
        return resultMap;
    }

    @PostMapping("/createOrg")
    public ResponseEntity<Map<String, Object>> createOrg(@RequestBody Map<String, Object> requestMap) {
        Map<String, Object> resultMap = new HashMap<>();
        String orgName = (String) requestMap.get("orgName");
        String userEmail = (String) requestMap.get("userEmail");
        String password = (String) requestMap.get("password");
        String userName = (String) requestMap.get("userName");

        if(OrgUtil.getInstance().isEmailExists(userEmail)) {
            resultMap.put(StatusConstants.STATUS_CODE, 409);
            resultMap.put(StatusConstants.MESSAGE, "user email already exist! try login");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultMap);
        }

        if(!orgService.validateOrgName(orgName)) {
            resultMap.put(StatusConstants.STATUS_CODE, 409);
            resultMap.put(StatusConstants.MESSAGE, "organization name already found!...");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultMap);
        }

        try {
            orgService.createOrg(orgName, userEmail, password, userName);
            resultMap.put(StatusConstants.STATUS_CODE, 200);
            resultMap.put(StatusConstants.MESSAGE, "Organization created successfully");
            return ResponseEntity.status(HttpStatus.OK).body(resultMap);
        } catch (Exception e) {
            resultMap.put(StatusConstants.STATUS_CODE, 400);
            resultMap.put(StatusConstants.MESSAGE, "unable to create organization! contact support");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultMap);
        }
    }
}
