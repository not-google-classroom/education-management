package com.org.education_management.model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

public class User {

    public static User user = null;
    private String userName;
    private String userEmail;
    private String gender;
    private int status;
    private long userID;
    private long roleID;
    private String roleName;
    LinkedList<String> permissionList;
    private long createdAt;
    private long updatedAt;
    boolean isChangePwd;

    public boolean isChangePwd() {
        return isChangePwd;
    }

    public void setChangePwd(boolean changePwd) {
        isChangePwd = changePwd;
    }

    public User() {

    }

    public User(Long userID, String userName, String userEmail, String roleName, long roleID, LinkedList<String> permissionsList, long createdAt, long updatedAt, String gender, int status, boolean isChangePwd) {
        this.userID = userID;
        this.userName = userName;
        this.userEmail = userEmail;
        this.roleName = roleName;
        this.roleID = roleID;
        this.permissionList = permissionsList;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.gender = gender;
        this.status = status;
        this.isChangePwd = isChangePwd;
    }

    public static User getInstance() {
        if(user == null) {
            user = new User();
        }
        return user;
    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        User.user = user;
    }

    public LinkedList<String> getPermissionList() {
        return permissionList;
    }

    public void setPermissionList(LinkedList<String> permissionList) {
        this.permissionList = permissionList;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public long getRoleID() {
        return roleID;
    }

    public void setRoleID(long roleID) {
        this.roleID = roleID;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public User setUserData(Long userID, Map<String, Object> userDetails) {
        Map userDetailsMap = (Map) userDetails.get(userID.toString());
        if(userDetailsMap != null && !userDetailsMap.isEmpty()) {
            String userName = (String) userDetailsMap.get("username");
            String userEmail = userDetailsMap.get("email").toString();
            long createdAt = (long) userDetailsMap.get("created_at");
            long updatedAt = (long) userDetailsMap.get("updated_at");
            String roleName = (String) userDetailsMap.get("role_name");
            long roleID = (long) userDetailsMap.get("role_id");
            String permissions = (String) userDetailsMap.get("permissions");
            String gender = (String) userDetailsMap.get("gender_type");
            int status = (int) userDetailsMap.get("status");
            boolean isChangePwd = (boolean) userDetailsMap.get("change_password");
            LinkedList<String> permissionsList = new LinkedList<>(Arrays.asList(permissions.split(",")));
            User user = new User(userID, userName, userEmail, roleName, roleID, permissionsList, createdAt, updatedAt, gender, status, isChangePwd);
            return user;
        }
         return null;
    }
}
