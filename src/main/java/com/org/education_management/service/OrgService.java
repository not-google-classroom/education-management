package com.org.education_management.service;

import com.org.education_management.database.DataBaseUtil;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jooq.impl.DSL.field;

public class OrgService {

    private static final Logger logger = Logger.getLogger(OrgService.class.getName());

    public Map<String, Object> getDetailsByOrgID(long orgID) {
        logger.log(Level.INFO, "OrgService : getting org details by ID called with id : {0}", orgID);

        // Retrieve DSLContext from the singleton DataBaseUtil
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
}