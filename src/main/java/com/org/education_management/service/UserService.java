package com.org.education_management.service;

import com.org.education_management.constants.UserConstants;
import com.org.education_management.model.User;
import com.org.education_management.model.UserContext;
import com.org.education_management.util.*;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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

    public Map<String, Object> addUser(Map<String, Object> requestMap, HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        boolean isPassChangeRequired = true;
        try {
            String userName = (String) requestMap.get("userName");
            String userEmail = (String) requestMap.get("userEmail");
            Long userRole = Long.parseLong(requestMap.get("userRole").toString());
            String ugIDs = (String) requestMap.get("ugIDs");
            int genderID = (Integer) requestMap.get("genderID");
            String reqAddr = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
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
                boolean isUserAdded = UserMgmtUtil.getInstance().addUser(userEmail, userName, "", userRole, ugList, true, genderID, UserConstants.USER_INVITED, reqAddr, isPassChangeRequired);
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

    public boolean inviteUser(Map<String, Object> requestMap) throws Exception {
        boolean isUserActivated = false;
        try {
            String token = (String) requestMap.get("token");
            Claims claims = JWTUtil.validateToken(token);
            String tokenSub = (String) claims.get("sub");
            byte[] decodedVal = Base64.getUrlDecoder().decode(tokenSub.getBytes(StandardCharsets.UTF_8));
            String decodedContent = new String(decodedVal);
            String[] splitContent = decodedContent.split(",");
            SchemaUtil.getInstance().setSearchPathForSchema(splitContent[2]);
            String userEmail = splitContent[1];
            int userStatus = UserMgmtUtil.getInstance().getUserStatus(userEmail);
            if (userStatus == UserConstants.USER_INVITED) {
                logger.log(Level.INFO, "Going to activate the user... {0}", MaskUtil.getInstance().maskEmail(userEmail));
                isUserActivated = UserMgmtUtil.getInstance().activateUser(userEmail);
            }
            logger.log(Level.INFO, "token received : ", token);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when inviting user to org! : {0}", e);
            e.printStackTrace();
            throw new Exception("Exception when Inviitng user to org");
        }
        return isUserActivated;
    }

    public boolean updatePassword(Map<String, Object> requestMap) throws Exception {
        boolean isPwdUpdated = false;
        User currentUser = UserContext.getUser();
        if(currentUser != null) {
            String newPwd = (String) requestMap.get("newPassword");
            String cnfrmPwd = (String) requestMap.get("confirmPassword");
            if (newPwd.equals(cnfrmPwd)) {
                isPwdUpdated = UserMgmtUtil.getInstance().updateUserPwd(newPwd, currentUser);
            } else {
                logger.log(Level.SEVERE, "both password and confirm password doesn't match!");
                throw new Exception("Passwords doesn't match!");
            }
        } else {
            logger.log(Level.SEVERE, "unable to find current loggedIn user!");
            throw new Exception("current loggedIn user, couldn't be found!");
        }
        return isPwdUpdated;
    }
}
