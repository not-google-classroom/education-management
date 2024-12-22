package com.org.education_management.util;

import com.org.education_management.database.DataBaseUtil;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

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
        if (adminRoleID != null) {
            isAdminCreated = addUser(userEmail, userName, password, adminRoleID);
        }
        return isAdminCreated;
    }

    public boolean addUser(String userEmail, String userName, String password, Long roleID) throws Exception {
        boolean isUserAdded = false;
        try {
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            Record record = dslContext.insertInto(table("users")).columns(field("username"), field("email"), field("created_at"), field("updated_at"))
                    .values(userName, userEmail, System.currentTimeMillis(), System.currentTimeMillis()).returning(field("user_id")).fetchSingle();
            if (record != null && record.size() > 0) {
                logger.log(Level.INFO, "User details added to database, proceeding with password and role mappings");
                Long userID = (Long) record.get(field("user_id"));
                if (password != null && !password.isEmpty()) {
                    addPasswordMappingForUser(userID, password);
                }
                addUsersRoleMapping(userID, roleID);
                isUserAdded = true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred when processing user addition : {0}", e);
            throw new Exception("Exception when creating user!");
        }
        return isUserAdded;
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
}
