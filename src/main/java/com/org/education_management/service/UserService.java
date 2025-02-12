package com.org.education_management.service;

import com.org.education_management.constants.UserConstants;
import com.org.education_management.util.RolesUtil;
import com.org.education_management.util.StatusConstants;
import com.org.education_management.util.UserMgmtUtil;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserService {

    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    public Map<String, Object> getUsers(Long userID) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Map usersMap = UserMgmtUtil.getInstance().getUsers(userID);
            if (!usersMap.isEmpty()) {
                resultMap.put(StatusConstants.STATUS_CODE, HttpStatus.OK);
                resultMap.put(StatusConstants.MESSAGE, "Users data fetched successfully");
                resultMap.put(StatusConstants.DATA, usersMap);
                return resultMap;
            }
            resultMap.put(StatusConstants.STATUS_CODE, HttpStatus.NO_CONTENT);
            resultMap.put(StatusConstants.MESSAGE, "No users found in the org");
            return resultMap;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when fetching user details ", e);
            resultMap.put(StatusConstants.STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
            resultMap.put(StatusConstants.MESSAGE, "Internal error when processing request");
            return resultMap;
        }
    }

    public Map<String, Object> addUser(Map<String, Object> requestMap) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            String userName = (String) requestMap.get("userName");
            String userEmail = (String) requestMap.get("userEmail");
            Long userRole = Long.parseLong(requestMap.get("userRole").toString());
            String ugIDs = (String) requestMap.get("ugIDs");
            int genderID = (Integer) requestMap.get("genderID");
            LinkedList<Long> ugList = new LinkedList<>();
            ugList.add(UserMgmtUtil.getInstance().getAllUsersUGID()); // All users group
            if (ugIDs != null && !ugIDs.isEmpty()) {
                try {
                    for (String id : ugIDs.split(",")) {
                        ugList.add(Long.parseLong(id.trim()));
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid ugID value provided. Expected numeric values separated by commas.", e);
                }
            }
            boolean emailExists = UserMgmtUtil.getInstance().isEmailExists(userEmail);
            if(emailExists) {
                resultMap.put(StatusConstants.STATUS_CODE, HttpStatus.CONFLICT);
                resultMap.put(StatusConstants.MESSAGE, "User Email already found! try login");
                return resultMap;
            }
            Map roleDetails = RolesUtil.getInstance().getRolesList(userRole);
            if (roleDetails != null && !roleDetails.isEmpty()) {
                boolean isUserAdded = UserMgmtUtil.getInstance().addUser(userEmail, userName, "", userRole, ugList, true, genderID, UserConstants.USER_INVITED);
                if(isUserAdded) {
                    resultMap.put(StatusConstants.STATUS_CODE, HttpStatus.OK);
                    resultMap.put(StatusConstants.MESSAGE, "User created successfully");
                    return resultMap;
                }
            } else {
                logger.log(Level.SEVERE, "Exception when user creation! Invalid role details passed for id : ", userRole);
                resultMap.put(StatusConstants.STATUS_CODE, HttpStatus.BAD_REQUEST);
                resultMap.put(StatusConstants.MESSAGE, "Invalid role details passed!");
                return resultMap;
            }
            logger.log(Level.SEVERE, "Unable to create user with request data");
            resultMap.put(StatusConstants.STATUS_CODE, HttpStatus.BAD_REQUEST);
            resultMap.put(StatusConstants.MESSAGE, "User creation failed! check logs");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when creating user!, trace : ", e);
            resultMap.put(StatusConstants.STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
            resultMap.put(StatusConstants.MESSAGE, "Internal server error unable to process request");
        }
        return resultMap;
    }

    public Map<String, Object> getUserGroups(Long ugID) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Map usersMap = UserMgmtUtil.getInstance().getUserGroups(ugID);
            if (!usersMap.isEmpty()) {
                resultMap.put(StatusConstants.STATUS_CODE, HttpStatus.OK);
                resultMap.put(StatusConstants.MESSAGE, "User Groups data fetched successfully");
                resultMap.put(StatusConstants.DATA, usersMap);
                return resultMap;
            }
            resultMap.put(StatusConstants.STATUS_CODE, HttpStatus.NO_CONTENT);
            resultMap.put(StatusConstants.MESSAGE, "No user groups found in the org");
            return resultMap;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when fetching user groups details ", e);
            resultMap.put(StatusConstants.STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
            resultMap.put(StatusConstants.MESSAGE, "Internal error when processing request");
            return resultMap;
        }
    }

    public boolean addUsersGroup(Map<String, Object> requestMap) throws Exception {
        boolean isUGCreated = false;
        try {
            if (requestMap != null && !requestMap.isEmpty()) {
                String ugName = (String) requestMap.get("ugName");
                int ugType = (Integer) requestMap.getOrDefault("ugType", 0);
                String ugDesc = (String) requestMap.getOrDefault("ugDesc", "--");
                if (ugType == 1) { //1 refers to dynamic group
                    long filterID = Long.parseLong(requestMap.get("filterID").toString());
                    long operatorID = Long.parseLong(requestMap.get("operatorID").toString());
                    String value = (String) requestMap.get("filterByValue");
                    isUGCreated = UserMgmtUtil.getInstance().createDynamicUserGroup(ugName, ugDesc, ugType, filterID, operatorID, value);
                } else if (ugType == 2) { // refers to static group
                    String userIDs = (String) requestMap.get("userIDs");
                    isUGCreated = UserMgmtUtil.getInstance().createStaticUserGroup(ugName, ugDesc, ugType, userIDs);
                }
            }
            return isUGCreated;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when adding user group : {0}", e);
            throw new Exception("Exception when adding usergroup data");
        }
    }
}
