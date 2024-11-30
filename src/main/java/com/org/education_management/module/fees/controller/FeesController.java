package com.org.education_management.module.fees.controller;

import com.org.education_management.module.fees.service.FeesService;
import com.org.education_management.util.StatusConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("api/fees")
public class FeesController {

    private static final Logger logger = Logger.getLogger(FeesController.class.getName());

    FeesService feesService = new FeesService();

    @PostMapping("/createFeesStructure")
    public Map<String, Object> createFeesStructure(@RequestParam Map<String, Object> feesDetails) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            JSONObject request = new JSONObject(feesDetails);
            feesService.createFeesStructure(request);

            // Success response
            resultMap.put(StatusConstants.STATUS_CODE, 200);
            resultMap.put(StatusConstants.MESSAGE, "Fees structure created successfully");
        } catch (Exception e) {
            // Error response
            resultMap.put(StatusConstants.STATUS_CODE, 400);
            resultMap.put(StatusConstants.MESSAGE, "Unable to create fees structure contact support");
        }
        return resultMap;
    }

    @GetMapping("/payFees")
    public Map<String, Object> payFees(@RequestParam Map<String, Object> requestMap) throws JSONException {
        Map<String, Object> resultMap = new HashMap<>();
        System.out.println(requestMap);
        try {
            if (feesService.payFees(requestMap)) {
                resultMap.put(StatusConstants.MESSAGE, "Organization details fetched successfully");
                resultMap.put(StatusConstants.STATUS_CODE, 200);
            } else {
                resultMap.put(StatusConstants.STATUS_CODE, 204);
                resultMap.put(StatusConstants.MESSAGE, "Unable to process the request!");
            }
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

    @PostMapping("/mapFees")
    public Map<String, Object> mapFees(@RequestParam Map<String, Object> feesDetails) throws JSONException {
        Map<String, Object> resultMap = new HashMap<>();
        System.out.println(feesDetails);
        try {
            if (feesService.mapFees(new JSONObject(feesDetails))) {
                resultMap.put(StatusConstants.MESSAGE, "Organization details fetched successfully");
                resultMap.put(StatusConstants.STATUS_CODE, 200);
            } else {
                resultMap.put(StatusConstants.STATUS_CODE, 204);
                resultMap.put(StatusConstants.MESSAGE, "Unable to process the request!");
            }
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

    @GetMapping("/getFeesForUser")
    public Map<String, Object> getFeesForUser(@RequestParam Map<String, Object> feesDetails) throws JSONException {
        Map<String, Object> resultMap = new HashMap<>();
        System.out.println(feesDetails);
        try {
            if (feesService.getFeesForUser(feesDetails)) {
                resultMap.put(StatusConstants.MESSAGE, "Organization details fetched successfully");
                resultMap.put(StatusConstants.STATUS_CODE, 200);
            } else {
                resultMap.put(StatusConstants.STATUS_CODE, 204);
                resultMap.put(StatusConstants.MESSAGE, "Unable to process the request!");
            }
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
}
