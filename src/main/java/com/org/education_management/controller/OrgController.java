package com.org.education_management.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.org.education_management.model.OrgDetails;
import com.org.education_management.repository.OrgDetailsRepository;
import com.org.education_management.util.StatusConstants;
import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("org")
public class OrgController {

    @Autowired
    private OrgDetailsRepository orgDetailsRepository;

    @GetMapping(value = "/getOrgDetails", produces = "application/json")
    private Map<String, Object> getOrgDetails(@RequestBody Object jsonDetails) throws JSONException {
        Map<String, Object> resultMap = new HashMap<>();
        LinkedHashMap requestMap = (LinkedHashMap) jsonDetails;
        System.out.println(requestMap);
        if(requestMap.containsKey("orgID") && requestMap.get("orgID") != null){
            Map<String, Object> orgDetailsMap = (Map<String, Object>) orgDetailsRepository.getReferenceById(Long.parseLong(requestMap.getOrDefault("orgID", 0L).toString()));
            resultMap.put(StatusConstants.MESSAGE, orgDetailsMap);
            resultMap.put(StatusConstants.STATUS, "Organization details fetched successfully");
            resultMap.put(StatusConstants.STATUS_CODE, 200);
        }
        resultMap.put(StatusConstants.STATUS_CODE, 204);
        resultMap.put(StatusConstants.STATUS, "No data found in request body!");
        resultMap.put(StatusConstants.MESSAGE, "Unable to process the request!");
        return resultMap;
    }
}
