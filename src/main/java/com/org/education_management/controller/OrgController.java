package com.org.education_management.controller;

import com.org.education_management.service.OrgService;
import com.org.education_management.util.StatusConstants;
import org.json.JSONException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/org")
public class OrgController {

    OrgService orgService = new OrgService();

    @GetMapping(value = "/getOrgDetails", produces = "application/json")
    private Map<String, Object> getOrgDetails(@RequestParam Map<String, Object> requestMap) throws JSONException {
        Map<String, Object> resultMap = new HashMap<>();
        System.out.println(requestMap);
        if(requestMap.containsKey("orgID") && requestMap.get("orgID") != null){
            Map<String, Object> orgDetailsMap = (Map<String, Object>) orgService.getDetailsByOrgID(Long.parseLong(requestMap.getOrDefault("orgID", 0L).toString()));
            resultMap.put(StatusConstants.DATA, orgDetailsMap);
            resultMap.put(StatusConstants.MESSAGE, "Organization details fetched successfully");
            resultMap.put(StatusConstants.STATUS_CODE, 200);
            return resultMap;
        }
        resultMap.put(StatusConstants.STATUS_CODE, 204);
        resultMap.put(StatusConstants.MESSAGE, "Unable to process the request!");
        return resultMap;
    }

    @PostMapping("/createOrg")
    private boolean createOrg(@RequestParam Map<String, Object> requestMap) {
        boolean isOrgCreated = false;

        return isOrgCreated;
    }
}
