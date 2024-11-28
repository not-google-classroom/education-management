package com.org.education_management.util;

import com.org.education_management.database.DataBaseUtil;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class RolesUtil {

    private static final Logger logger = Logger.getLogger(RolesUtil.class.getName());
    private static RolesUtil rolesUtil;

    public static RolesUtil getInstance() {
        if(rolesUtil == null) {
            rolesUtil = new RolesUtil();
        }
        return rolesUtil;
    }

    public Map<String, Object> getRolesList(Long roleID) {
        Map<String, Object> rolesMap = new HashMap<>();
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Result<Record> result = dslContext.selectFrom(table("roles")).where(roleID != null ? field("role_id").eq(roleID) : DSL.noCondition()).fetch();
        for (Record record : result) {
            rolesMap.put((String) record.get("role_name"), record.intoMap());
        }
        return rolesMap;
    }
}
