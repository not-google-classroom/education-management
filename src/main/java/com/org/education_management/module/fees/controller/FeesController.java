package com.org.education_management.module.fees.controller;

import com.org.education_management.module.fees.service.FeesService;
import com.org.education_management.util.StatusConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.org.education_management.module.fees.controller.ResponseEntityWrapper.buildResponse;

@RestController
@RequestMapping("api/fees")
public class FeesController {
    private static final Logger logger = Logger.getLogger(FeesController.class.getName());
    FeesService feesService = new FeesService();

    @PostMapping("/createFeesStructure")
    public ResponseEntity<Map<String, Object>> createOrg(@RequestBody Map<String, Object> feesDetails) {
        try {
            JSONObject request = new JSONObject(feesDetails);
            if (feesService.createFeesStructure(request)) {
                return buildResponse(HttpStatus.OK, "Fees structure created successfully");
            } else {
                return buildResponse(HttpStatus.BAD_REQUEST, "Unable to create fees structure, contact support");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred while createFeesStructure : {0}", e);
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/payFees")
    public ResponseEntity<Map<String, Object>> payFees(@RequestParam Map<String, Object> requestMap) throws JSONException {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if (feesService.payFees(requestMap)) {
                return buildResponse(HttpStatus.OK, "Fees paid successfully");
            } else {
                return buildResponse(HttpStatus.BAD_REQUEST, "Unable to pay fees, contact support");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred while payFees : {0}", e);
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/mapFees")
    public ResponseEntity<Map<String, Object>> mapFees(@RequestParam Map<String, Object> feesDetails) throws JSONException {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if (feesService.mapFees(new JSONObject(feesDetails))) {
                return buildResponse(HttpStatus.OK, "Fees mapped successfully");
            } else {
                return buildResponse(HttpStatus.BAD_REQUEST, "Unable to map fees, contact support");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred while mapFees : {0}", e);
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getAllFees")
    public ResponseEntity<Map<String, Object>> getAllFees() throws JSONException {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            return buildResponse(HttpStatus.OK, "All Fees fetched", feesService.getAllFees());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred while getAllFees : {0}", e);
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getFeesIdsForUser")
    public ResponseEntity<Map<String, Object>> getFeesIdsForUser(@RequestParam Map<String, Object> feesDetails) throws JSONException {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            return buildResponse(HttpStatus.OK, "Fees fetched for user", feesService.getFeesIdsForUser(feesDetails));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred while getFeesForUser : {0}", e);
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getBalanceFeesForUser")
    public ResponseEntity<Map<String, Object>> getBalanceFees(@RequestParam Map<String, Object> feesDetails) throws JSONException {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            return buildResponse(HttpStatus.OK, "Balance fees fetched for user", feesService.getBalanceFeesForUser(feesDetails));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred while getBalanceFees : {0}", e);
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/createFine")
    public ResponseEntity<Map<String, Object>> createFine(@RequestParam Map<String, Object> fineDetails) throws JSONException {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if (feesService.createFine(fineDetails)) {
                return buildResponse(HttpStatus.OK, "Fine created successfully");
            } else {
                resultMap.put(StatusConstants.STATUS_CODE, 400);
                return buildResponse(HttpStatus.BAD_REQUEST, "Unable to create fine, contact support");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred while createFine : {0}", e);
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/mapFineToUsers")
    public ResponseEntity<Map<String, Object>> mapFineToUsers(@RequestParam Map<String, Object> fineDetails) throws JSONException {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if (feesService.mapFineToUsers(fineDetails)) {
                return buildResponse(HttpStatus.OK, "Fine mapped to users");
            } else {
                resultMap.put(StatusConstants.STATUS_CODE, 400);
                return buildResponse(HttpStatus.BAD_REQUEST, "Unable to map fine, contact support");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred while mapFineToUsers : {0}", e);
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
