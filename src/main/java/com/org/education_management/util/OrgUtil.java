package com.org.education_management.util;

import com.org.education_management.database.DataBaseUtil;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class OrgUtil {

    private static final Logger logger = Logger.getLogger(OrgUtil.class.getName());
    public static OrgUtil orgUtil = null;

    public static OrgUtil getInstance() {
        if (orgUtil == null) {
            orgUtil = new OrgUtil();
        }
        return orgUtil;
    }

    public boolean isEmailExists(String userEmail) {
        boolean isExists = false;
        if(userEmail != null && !userEmail.isEmpty()) {
            DSLContext dsl = DataBaseUtil.getDSLContext();
            Record record = dsl.select()
                    .from("UserDetails")
                    .where(field("USER_EMAIL").eq(userEmail))
                    .fetchOne();
            if(record != null && record.size() > 0) {
                isExists = true;
            }
        }
        return isExists;
    }

    private void addEntryToUserDetails(String userEmail) throws Exception {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        int record = dslContext.insertInto(table("UserDetails")).columns(field("USER_EMAIL"), field("CREATED_TIME")).values(userEmail, System.currentTimeMillis()).execute();
        if(record > 0) {
            logger.log(Level.INFO, "user added successfully...");
        } else {
            logger.log(Level.SEVERE, "user addition failed");
            throw new Exception("Exception when executing insert for UserDetails");
        }
    }

    public Long getUserIDByEmail(String userEmail) {
        Long userID = null;
        if(userEmail != null && !userEmail.isEmpty()) {
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            Record record = dslContext.select().from("UserDetails").where(field("USER_EMAIL").eq(userEmail)).fetchOne();
            if(record != null) {
                userID = (Long) record.get(0);
            }
        }
        return userID;
    }

    public void createOrgAndSchema(String orgName, String userEmail) throws Exception {
        try{
            addEntryToUserDetails(userEmail);
            addEntryToOrgDetailsAndMapping(orgName, userEmail);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred when adding entry to table : {0}", e);
            throw new Exception(e);
        }
    }

    private void addEntryToOrgDetailsAndMapping(String orgName, String userEmail) throws Exception {
        Long userID = getUserIDByEmail(userEmail);
        if(userID != null) {
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            int addOrgRecord = dslContext.insertInto(table("OrgDetails")).columns(field("ORG_NAME"), field("USER_ID"), field("CREATED_TIME")).values(orgName, userID, System.currentTimeMillis()).execute();
            if(addOrgRecord > 0) {
                logger.log(Level.INFO, "organization : {0}, added successfully, mapping and schema creation starts.");
                addMappingsForOrgAndUser(orgName, userID);
            }
        } else {
            throw new Exception("User not found! unable to create org");
        }
    }

    private void addMappingsForOrgAndUser(String orgName, Long userID) {
        Long orgID = getOrgIDByUserID(userID);
        if(orgID != null && userID != null) {
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            int addMapping = dslContext.insertInto(table("OrgUsersMapping")).columns(field("ORG_ID"), field("USER_ID")).values(orgID, userID).execute();
            if(addMapping > 0) {
                logger.log(Level.INFO, "Org users mapping added successfully for org : {0}", orgName);
            }
        }
    }

    public Long getOrgIDByUserID(Long userID) {
        Long orgID = null;
        if(userID != null) {
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            Record orgRecord = dslContext.select().from("OrgDetails").where(field("USER_ID").eq(userID)).fetchOne();
            if(orgRecord != null) {
                orgID = (Long) orgRecord.get(0);
            }
        }
        return orgID;
    }

    public Map<String, Object> getOrgDetailsByID(long orgID) {
        DSLContext dsl = DataBaseUtil.getDSLContext();

        // Execute query
        Record record = dsl.select()
                .from("OrgDetails")
                .where(field("ORG_ID").eq(orgID))
                .fetchOne();

        Map<String, Object> resultMap = new HashMap<>();

        if (record != null) {
            resultMap = record.intoMap();
        }

        logger.log(Level.INFO, "Result record : {0}", resultMap);
        return resultMap;

    }

    public void deletePrepopulatedDataForSchemaFailure(Long orgID, Long userID) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        SchemaUtil.getInstance().setSearchPathToPublic();
        if(orgID != null) {
            dslContext.deleteFrom(table("OrgDetails")).where(field("ORG_ID").eq(orgID)).execute();
            if(userID != null) {
                dslContext.deleteFrom(table("UserDetails")).where(field("USER_ID").eq(userID)).execute();
            }
            logger.log(Level.INFO, "org and user details deleted successfully, try creating new organization");
        }
    }

    public Map<String, Object> getOrgDetailsByName(String orgName) {
        DSLContext dsl = DataBaseUtil.getDSLContext();

        // Execute query
        Record record = dsl.select()
                .from("OrgDetails")
                .where(field("ORG_NAME").eq(orgName))
                .fetchOne();

        Map<String, Object> resultMap = new HashMap<>();

        if (record != null) {
            resultMap = record.intoMap();
        }

        logger.log(Level.INFO, "Result record : {0}", resultMap);
        return resultMap;
    }
}
