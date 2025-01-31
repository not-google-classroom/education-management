package com.org.education_management.service;

import com.org.education_management.util.RolesUtil;
import com.org.education_management.util.StatusConstants;
import com.org.education_management.util.UserMgmtUtil;

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
                resultMap.put(StatusConstants.STATUS_CODE, 200);
                resultMap.put(StatusConstants.MESSAGE, "Users data fetched successfully");
                resultMap.put(StatusConstants.DATA, usersMap);
                return resultMap;
            }
            resultMap.put(StatusConstants.STATUS_CODE, 204);
            resultMap.put(StatusConstants.MESSAGE, "No users found in the org");
            return resultMap;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when fetching user details ", e);
            resultMap.put(StatusConstants.STATUS_CODE, 500);
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
                resultMap.put(StatusConstants.STATUS_CODE, 409);
                resultMap.put(StatusConstants.MESSAGE, "User Email already found! try login");
                return resultMap;
            }
            Map roleDetails = RolesUtil.getInstance().getRolesList(userRole);
            if (roleDetails != null && !roleDetails.isEmpty()) {
                boolean isUserAdded = UserMgmtUtil.getInstance().addUser(userEmail, userName, "", userRole, ugList, true);
                if(isUserAdded) {
                    resultMap.put(StatusConstants.STATUS_CODE, 200);
                    resultMap.put(StatusConstants.MESSAGE, "User created successfully");
                    return resultMap;
                }
            } else {
                logger.log(Level.SEVERE, "Exception when user creation! Invalid role details passed for id : ", userRole);
                resultMap.put(StatusConstants.STATUS_CODE, 400);
                resultMap.put(StatusConstants.MESSAGE, "Invalid role details passed!");
                return resultMap;
            }
            logger.log(Level.SEVERE, "Unable to create user with request data");
            resultMap.put(StatusConstants.STATUS_CODE, 400);
            resultMap.put(StatusConstants.MESSAGE, "User creation failed! check logs");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when creating user!, trace : ", e);
            resultMap.put(StatusConstants.STATUS_CODE, 500);
            resultMap.put(StatusConstants.MESSAGE, "Internal server error unable to process request");
        }
        return resultMap;
    }

    public Map<String, Object> getUserGroups(Long ugID) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Map usersMap = UserMgmtUtil.getInstance().getUserGroups(ugID);
            if (!usersMap.isEmpty()) {
                resultMap.put(StatusConstants.STATUS_CODE, 200);
                resultMap.put(StatusConstants.MESSAGE, "User Groups data fetched successfully");
                resultMap.put(StatusConstants.DATA, usersMap);
                return resultMap;
            }
            resultMap.put(StatusConstants.STATUS_CODE, 204);
            resultMap.put(StatusConstants.MESSAGE, "No user groups found in the org");
            return resultMap;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when fetching user groups details ", e);
            resultMap.put(StatusConstants.STATUS_CODE, 500);
            resultMap.put(StatusConstants.MESSAGE, "Internal error when processing request");
            return resultMap;
        }
    }

    public Map<String, Object> addUsersGroup(Map<String, Object> requestMap) {
        return null;
    }
}
