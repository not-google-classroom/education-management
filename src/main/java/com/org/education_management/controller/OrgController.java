package com.org.education_management.controller;

import com.org.education_management.util.StatusConstants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController()
public class OrgController {

    @GetMapping("/org")
    private Map<String, Object> getOrgDetails(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> resultMap = new HashMap<>();
        if(requestData != null && !requestData.isEmpty()) {

        }
        resultMap.put(StatusConstants.STATUS_CODE, 204);
        resultMap.put(StatusConstants.STATUS, "No data found in request body!");
        resultMap.put(StatusConstants.MESSAGE, "Unable to process the request!");
        return resultMap;
    }
}
