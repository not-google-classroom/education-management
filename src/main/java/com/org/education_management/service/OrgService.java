package com.org.education_management.service;

import com.org.education_management.util.OrgUtil;
import com.org.education_management.util.SchemaUtil;
import com.org.education_management.util.UniqueValueGenerator;
import com.org.education_management.util.UserMgmtUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrgService {

    private static final Logger logger = Logger.getLogger(OrgService.class.getName());

    public Map<String, Object> getDetailsByOrgID(long orgID) throws Exception {
        logger.log(Level.INFO, "OrgService : getting org details by ID called with id : {0}", orgID);
        Map<String, Object> resultMap = new HashMap<>();
        try {
            resultMap = OrgUtil.getInstance().getOrgDetailsByID(orgID);
        } catch (Exception e) {
            throw new Exception("Exception when getting orgdetails by ID : {0}, with Exception : {1}");
        }
        return resultMap;
    }

    public boolean validatePasswords(String password, String confirmPassword) {
        if(password != null && confirmPassword != null) {
            return password.equalsIgnoreCase(confirmPassword);
        }
        return false;
    }

    public void createOrg(String orgName, String userEmail, String password, String firstName, String lastName) throws Exception {
        String userName = firstName;
        if(!lastName.isEmpty()) {
            userName = userName + " " + lastName;
        }
        try {
            OrgUtil.getInstance().createOrgAndSchema(orgName, userEmail);
            String schemaName = SchemaUtil.getInstance().createSchemaAndPopulateData(userEmail);
            if(schemaName != null && !schemaName.isEmpty()) {
                UserMgmtUtil.getInstance().addAdminToOrg(schemaName, orgName, userEmail, password, userName, firstName, lastName);
                //UniqueValueGenerator.getInstance().updateValuesToDB(); getting error
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when creating organization : {0}", e);
            throw new Exception(e);
        }
    }

    public boolean validateOrgName(String orgName) {
        if(orgName != null && !orgName.isEmpty()) {
            Map<String, Object> orgDetails = OrgUtil.getInstance().getOrgDetailsByName(orgName);
            if(!orgDetails.isEmpty()) {
                logger.log(Level.WARNING,"Validation failed, organization already found!, contact org admin");
                return false;
            }
        }
        return true;
    }
}