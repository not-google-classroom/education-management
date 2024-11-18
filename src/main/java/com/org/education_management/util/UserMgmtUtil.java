package com.org.education_management.util;

import com.org.education_management.database.DataBaseUtil;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.QOM;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class UserMgmtUtil {

    private static final Logger logger = Logger.getLogger(UserMgmtUtil.class.getName());
    public static UserMgmtUtil userMgmtUtil = null;

    public static UserMgmtUtil getInstance() {
        if(userMgmtUtil == null) {
            userMgmtUtil = new UserMgmtUtil();
        }
        return userMgmtUtil;
    }

    public boolean addAdminToOrg(String schemaName, String orgName, String userEmail, String password, String userName, String firstName, String lastName) {
        boolean isAdminCreated = false;
        SchemaUtil.getInstance().setSearchPathForSchema(schemaName);
        Long adminRoleID = getAdminRoleID();
        if(adminRoleID != null) {
            isAdminCreated = addUser(userEmail, userName, password, firstName, lastName, adminRoleID);
        }
        return isAdminCreated;
    }

    public boolean addUser(String userEmail, String userName, String password, String firstName, String lastName, Long roleID) {
        boolean isUserAdded = false;
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Record record = dslContext.insertInto(table("users")).columns(field("username"), field("email"), field("first_name"), field("last_name"), field("created_at"), field("updated_at")).values(userName, userEmail, firstName, lastName, System.currentTimeMillis(), System.currentTimeMillis()).returning(field("user_id")).fetchSingle();
        if(record != null && record.size() > 0) {
            logger.log(Level.INFO, "User details added to database, proceeding with password and role mappings");
            Long userID = (Long) record.get(field("user_id"));
            addUsersRoleMapping(userID, roleID);
            isUserAdded = true;
        }
        return isUserAdded;
    }

    private void addUsersRoleMapping(Long userID, Long roleID) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        if(userID != null && roleID != null) {

        }
    }

    public Long getAdminRoleID() {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        org.jooq.Record record = dslContext.select(field("role_id")).from(table("roles")).where(field("role_name").eq("Admin")).fetchOne();
        return (record != null && record.get(field("role_id")) != null) ? (Long) record.get(field("role_id")) : null;
    }

}
