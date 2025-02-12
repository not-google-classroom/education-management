package com.org.education_management.util;

import com.org.education_management.constants.UserConstants;
import com.org.education_management.database.DataBaseUtil;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;

public class UserMgmtUtil {

    private static final Logger logger = Logger.getLogger(UserMgmtUtil.class.getName());
    public static UserMgmtUtil userMgmtUtil = null;

    public static UserMgmtUtil getInstance() {
        if (userMgmtUtil == null) {
            userMgmtUtil = new UserMgmtUtil();
        }
        return userMgmtUtil;
    }

    public boolean addAdminToOrg(String schemaName, String orgName, String userEmail, String password, String userName) throws Exception {
        boolean isAdminCreated = false;
        SchemaUtil.getInstance().setSearchPathForSchema(schemaName);
        Long adminRoleID = getAdminRoleID();
        Long allCGID = getAllUsersUGID();
        if (adminRoleID != null && allCGID != null) {
            LinkedList<Long> allCGIDs = new LinkedList<>();
            allCGIDs.add(allCGID);
            isAdminCreated = addUser(userEmail, userName, password, adminRoleID, allCGIDs, false, UserConstants.GENDER_OTHERS, UserConstants.USER_ACTIVE);
        }
        return isAdminCreated;
    }

    public Long getAllUsersUGID() {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        org.jooq.Record record = dslContext.select(field(name("usergroup", "ug_id"))).from(table("usergroup")).where(field(name("usergroup", "ug_name")).eq("All Users Group")).fetchOne();
        return (record != null && record.get(field(name("usergroup", "ug_id"))) != null) ? (Long) record.get(field(name("usergroup", "ug_id"))) : null;
    }

    public boolean addUser(String userEmail, String userName, String password, Long roleID, LinkedList<Long> ugIDs, boolean isPublicPopulate, int genderID, int status) throws Exception {
        boolean isUserAdded = false;
        try {
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            Record record = dslContext.insertInto(table("users")).columns(field("username"), field("email"), field("gender_id"), field("status"), field("created_at"), field("updated_at"))
                    .values(userName, userEmail, genderID, status, System.currentTimeMillis(), System.currentTimeMillis()).returning(field("user_id")).fetchSingle();
            if (record != null && record.size() > 0) {
                logger.log(Level.INFO, "User details added to database, proceeding with password and role mappings");
                Long userID = (Long) record.get(field("user_id"));
                if (password != null && !password.isEmpty()) {
                    addPasswordMappingForUser(userID, password);
                }
                addUsersRoleMapping(userID, roleID);
                addUsersUGMapping(userID, ugIDs);
                isUserAdded = true;
                if (isPublicPopulate) {
                    addUserDetailsInPublic(userEmail, userName);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred when processing user addition : {0}", e);
            throw new Exception("Exception when creating user!");
        }
        return isUserAdded;
    }

    private void addUserDetailsInPublic(String userEmail, String userName) throws Exception {
        String searchPath = SchemaUtil.getInstance().getSearchPatch();
        try {
            if (searchPath != null) {
                SchemaUtil.getInstance().setSearchPathToPublic();
                OrgUtil.getInstance().addEntryToUserDetails(userEmail);
                Long orgID = OrgUtil.getInstance().getOrgIDByUserID(getLoggedInAdminUserID());
                OrgUtil.getInstance().addEntryToOrgUsers(orgID, OrgUtil.getInstance().getUserIDByEmail(userEmail));

            }
        } finally {
            SchemaUtil.getInstance().setSearchPathForSchema(searchPath);
        }
    }

    private Long getLoggedInAdminUserID() {
        return 1L;
    }

    private void addPasswordMappingForUser(Long userID, String password) throws Exception {
        if (userID != null) {
            String hashedPassword = PasswordUtil.hashPassword(password);
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            dslContext.insertInto(table("passwords")).columns(field("user_id"), field("hashed_password"), field("created_at"), field("updated_at"))
                    .values(userID, hashedPassword, System.currentTimeMillis(), System.currentTimeMillis()).execute();
            logger.log(Level.INFO, "password mapping and hashing completed.");
        }
    }

    private void addUsersRoleMapping(Long userID, Long roleID) throws Exception {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        if (userID != null && roleID != null) {
            try {
                dslContext.insertInto(table("userroles")).columns(field("user_id"), field("role_id"))
                        .values(userID, roleID).execute();
                logger.log(Level.INFO, "UserRole mapping added to database");
            } catch (Exception e) {
                throw new RuntimeException("Exception when mapping user role ! {0}", e);
            }
        }
    }

    public Long getAdminRoleID() {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        org.jooq.Record record = dslContext.select(field("role_id")).from(table("roles")).where(field("role_name").eq("Admin")).fetchOne();
        return (record != null && record.get(field("role_id")) != null) ? (Long) record.get(field("role_id")) : null;
    }

    public boolean validateUser(String userEmail, String password) {
        if (userEmail != null && password != null) {
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            Record record = dslContext.select().from(table("users")).innerJoin(table("passwords")).on(field(name("passwords", "user_id")).eq(field(name("users", "user_id")))).where(field(name("users", "email")).eq(userEmail)).fetchOne();
            if (record != null) {
                String hashedPwd = (String) record.get(name("passwords", "hashed_password"));
                if (PasswordUtil.checkPassword(password, hashedPwd)) {
                    logger.log(Level.INFO, "validation successfully done for login.");
                    return true;
                }
                logger.log(Level.WARNING, "passwords doesn't match, unable to proceed further! Try again");
            } else {
                logger.log(Level.WARNING, "userdetails not found to initiate login !");
                return false;
            }
        }
        return false;
    }

    public Map<String, Object> getUsers(Long userID) {
        Map<String, Object> usersMap = new HashMap<>();
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Result<? extends Record> result = dslContext.select(field(name("users", "username")), field(name("users", "email")), field(name("users", "created_at")), field(name("users", "updated_at")), field(name("roles", "role_name")), field(name("roles", "description"))).from(table("users")).innerJoin(table("userroles")).on(field(name("users", "user_id")).eq(field(name("userroles", "user_id")))).innerJoin(table("roles")).on(field(name("roles", "role_id")).eq(field(name("userroles", "role_id")))).where(userID != null ? field(name("users", "user_id")).eq(userID) : DSL.noCondition()).fetch();
        for (Record record : result) {
            usersMap.put((String) record.get("username"), record.intoMap());
        }
        return usersMap;
    }

    public Map<Long, Object> getAllUsers() {
        Map<Long, Object> usersMap = new HashMap<>();
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Result<? extends Record> result = dslContext.select(field(name("users", "user_id")), field(name("users", "username")), field(name("users", "email")), field(name("users", "created_at")), field(name("users", "updated_at")), field(name("roles", "role_name")), field(name("roles", "description"))).from(table("users")).innerJoin(table("userroles")).on(field(name("users", "user_id")).eq(field(name("userroles", "user_id")))).innerJoin(table("roles")).on(field(name("roles", "role_id")).eq(field(name("userroles", "role_id")))).fetch();
        for (Record record : result) {
            usersMap.put((Long) record.get("user_id"), record.intoMap());
        }
        return usersMap;
    }

    public boolean isEmailExists(String userEmail) {
        boolean isExists = false;
        if (userEmail != null && !userEmail.isEmpty()) {
            DSLContext dsl = DataBaseUtil.getDSLContext();
            Record record = dsl.select()
                    .from("users")
                    .where(field("email").eq(userEmail))
                    .fetchOne();
            if (record != null && record.size() > 0) {
                isExists = true;
            }
        }
        return isExists;
    }

    public Map getUserGroups(Long ugID) {
        Map<String, Object> usersMap = new HashMap<>();
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Result<? extends Record> result = dslContext.select(field(name("usergroup", "ug_name")), field(name("usergroup", "ug_desc")), field(name("usergroup", "ug_type")), field(name("usergroup", "created_at")), field(name("usergroup", "updated_at"))).from(table("usergroup")).innerJoin(table("usersugmapping")).on(field(name("usergroup", "ug_id")).eq(field(name("usersugmapping", "ug_id")))).where(ugID != null ? field(name("usergroup", "ug_id")).eq(ugID) : DSL.noCondition()).fetch();
        for (Record record : result) {
            usersMap.put((String) record.get("ug_name"), record.intoMap());
        }
        return usersMap;
    }

    public boolean createStaticUserGroup(String ugName, String ugDesc, int ugType, String userIDs) throws Exception {
        boolean isGroupCreated = false;
        if (!isUGDetailsExists(ugName)) {
            logger.log(Level.INFO, "Adding a static user group with name : {0}", ugName);
            Long ugID = addUserGroupDetails(ugName, ugDesc, ugType);
            if (ugID != null) {
                LinkedList<Long> userIdsList = Arrays.stream(userIDs.split(","))
                        .map(Long::parseLong)
                        .collect(Collectors.toCollection(LinkedList::new));
                Map<Long, Object> allUsersList = getAllUsers();
                if (allUsersList.keySet().containsAll(userIdsList)) {
                    addUsersUGMapping(userIdsList, ugID);
                    logger.log(Level.INFO, "Static group created successfully");
                    return true;
                }
                logger.log(Level.WARNING, "Provided usersIDs are not valid!, userIDs : {0}", userIdsList);
            }
            logger.log(Level.WARNING, "unable to create static group");
            return isGroupCreated;
        }
        logger.log(Level.SEVERE, "user group details exists already! try with new unique name");
        return isGroupCreated;
    }

    private void addUsersUGMapping(LinkedList<Long> userIdsList, Long ugID) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        InsertValuesStepN<?> insertStep = (InsertValuesStepN<?>) dslContext.insertInto(table("usersugmapping", field("user_id", field("ug_id"))));
        for (Long userID : userIdsList) {
            insertStep.values(userID, ugID);
        }
        insertStep.execute();
        logger.log(Level.INFO, "Users UG Mapping added successfully");
    }

    private void addUsersUGMapping(Long userID, LinkedList<Long> ugIDs) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        InsertValuesStepN<?> insertStep = (InsertValuesStepN<?>) dslContext.insertInto(table("usersugmapping", field("user_id", field("ug_id"))));
        for (Long cgID : ugIDs) {
            insertStep.values(userID, cgID);
        }
        insertStep.execute();
        logger.log(Level.INFO, "Users UG Mapping added successfully");
    }

    private Long addUserGroupDetails(String ugName, String ugDesc, int ugType) throws Exception {
        Long ugID = null;
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        if (ugName != null && ugType != 0) {
            long currTime = System.currentTimeMillis();
            Record record = dslContext.insertInto(table("usergroup")).columns(field("ug_name"), field("ug_desc"), field("ug_type"), field("created_at"), field("updated_at"))
                    .values(ugName, ugDesc, ugType, currTime, currTime).returning(field("ug_id")).fetchSingle();
            if (record != null && record.size() > 0) {
                ugID = (Long) record.get("ug_id");
                logger.log(Level.INFO, "Usergroup Details added to database");
            }
        }
        return ugID;
    }

    private boolean isUGDetailsExists(String ugName) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Record record = dslContext.select(field(name("usergroup", "ug_name"))).from(table("usergroup")).where(ugName != null ? field(name("usergroup", "ug_name")).eq(ugName) : DSL.noCondition()).fetchOne();
        if (record != null) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public boolean createDynamicUserGroup(String ugName, String ugDesc, int ugType, long filterID, long operatorID, String value) throws Exception {
        boolean isGroupCreated = false;
        if (!isUGDetailsExists(ugName)) {
            logger.log(Level.INFO, "Adding a dynamic user group with name : {0}", ugName);
            Long ugID = addUserGroupDetails(ugName, ugDesc, ugType);
            if (ugID != null) {
                Long sFilterID = mapFilterDetailsForUG(filterID, operatorID, value);
                mapUGAndFilters(ugID, sFilterID);
                logger.log(Level.INFO, "Dynamic user group created successfully");
                isGroupCreated = true;
            }
        }
        return isGroupCreated;
    }

    private void mapUGAndFilters(Long ugID, Long sFilterID) throws Exception {
        if(ugID != null && sFilterID != null) {
            logger.log(Level.INFO, "usergroup filter mapping started...");
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            dslContext.insertInto(table("ugfiltermapping")).columns(field("ug_id"), field("filter_id")).values(ugID, sFilterID).execute();
            logger.log(Level.INFO, "usergroup filter mapping ended...");
        }
    }

    private Long mapFilterDetailsForUG(long filterID, long operatorID, String value) throws Exception {
        Long sFilterID = null;
        if (value != null && !value.isEmpty()) {
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            Record record = dslContext.insertInto(table("savedugfilters")).columns(field("filter_id"), field("filter_operator"), field("filter_by_value"))
                    .values(filterID, operatorID, value).returning(field("s_filter_id")).fetchSingle();
            if (record != null && record.size() > 0) {
                sFilterID = (Long) record.get("s_filter_id");
                logger.log(Level.INFO, "Filter Details added successfully");
            }
        }
        return sFilterID;
    }
}
