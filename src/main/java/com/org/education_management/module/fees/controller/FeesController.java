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
            if (feesService.createFeesStructure(request)) {
                resultMap.put(StatusConstants.STATUS_CODE, 200);
                resultMap.put(StatusConstants.MESSAGE, "Fees structure created successfully");
            } else {
                resultMap.put(StatusConstants.STATUS_CODE, 400);
                resultMap.put(StatusConstants.MESSAGE, "Unable to create fees structure, contact support");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred while createFeesStructure : {0}", e);
            resultMap.put(StatusConstants.STATUS_CODE, 500);
        }
        return resultMap;
    }

    @GetMapping("/payFees")
    public Map<String, Object> payFees(@RequestParam Map<String, Object> requestMap) throws JSONException {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if (feesService.payFees(requestMap)) {
                resultMap.put(StatusConstants.MESSAGE, "Fees paid successfully");
                resultMap.put(StatusConstants.STATUS_CODE, 200);
            } else {
                resultMap.put(StatusConstants.STATUS_CODE, 400);
                resultMap.put(StatusConstants.MESSAGE, "Unable to pay fees, contact support");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred while payFees : {0}", e);
            resultMap.put(StatusConstants.STATUS_CODE, 500);
        }
        return resultMap;
    }

    @PostMapping("/mapFees")
    public Map<String, Object> mapFees(@RequestParam Map<String, Object> feesDetails) throws JSONException {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if (feesService.mapFees(new JSONObject(feesDetails))) {
                resultMap.put(StatusConstants.MESSAGE, "Fees mapped successfully");
                resultMap.put(StatusConstants.STATUS_CODE, 200);
            } else {
                resultMap.put(StatusConstants.STATUS_CODE, 400);
                resultMap.put(StatusConstants.MESSAGE, "Unable to map fees, contact support");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred while mapFees : {0}", e);
            resultMap.put(StatusConstants.STATUS_CODE, 500);
        }
        return resultMap;
    }

    @GetMapping("/getFeesIdsForUser")
    public Map<String, Object> getFeesIdsForUser(@RequestParam Map<String, Object> feesDetails) throws JSONException {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            resultMap.put(StatusConstants.DATA, feesService.getFeesIdsForUser(feesDetails));
            resultMap.put(StatusConstants.MESSAGE, "Fees fetched for user");
            resultMap.put(StatusConstants.STATUS_CODE, 200);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred while getFeesForUser : {0}", e);
            resultMap.put(StatusConstants.STATUS_CODE, 500);
        }
        return resultMap;
    }

    @GetMapping("/getBalancFeesForUser")
    public Map<String, Object> getBalancFees(@RequestParam Map<String, Object> feesDetails) throws JSONException {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            resultMap.put(StatusConstants.DATA, feesService.getBalanceFeesForUser(feesDetails));
            resultMap.put(StatusConstants.MESSAGE, "Balance fees fetched for user");
            resultMap.put(StatusConstants.STATUS_CODE, 200);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred while getBalancFees : {0}", e);
            resultMap.put(StatusConstants.STATUS_CODE, 500);
        }
        return resultMap;
    }

    @GetMapping("/createFine")
    public Map<String, Object> createFine(@RequestParam Map<String, Object> fineDetails) throws JSONException {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if (feesService.createFine(fineDetails)) {
                resultMap.put(StatusConstants.MESSAGE, "Fine created successsfully");
                resultMap.put(StatusConstants.STATUS_CODE, 200);
            } else {
                resultMap.put(StatusConstants.STATUS_CODE, 400);
                resultMap.put(StatusConstants.MESSAGE, "Unable to create fine, contact support");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred while createFine : {0}", e);
            resultMap.put(StatusConstants.STATUS_CODE, 500);
        }
        return resultMap;
    }

    @PostMapping("/mapFineToUsers")
    public Map<String, Object> mapFineToUsers(@RequestParam Map<String, Object> fineDetails) throws JSONException {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if (feesService.mapFineToUsers(fineDetails)) {
                resultMap.put(StatusConstants.MESSAGE, "Fine mapped to users");
                resultMap.put(StatusConstants.STATUS_CODE, 200);
            } else {
                resultMap.put(StatusConstants.STATUS_CODE, 400);
                resultMap.put(StatusConstants.MESSAGE, "Unable to map fine, contact support");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred while mapFineToUsers : {0}", e);
            resultMap.put(StatusConstants.STATUS_CODE, 500);
        }
        return resultMap;
    }
}
