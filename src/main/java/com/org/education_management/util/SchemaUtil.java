package com.org.education_management.util;

import com.org.education_management.database.DataBaseUtil;
import com.org.education_management.service.StartUpService;
import org.jooq.DSLContext;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class SchemaUtil {
    private static final Logger logger = Logger.getLogger(SchemaUtil.class.getName());
    public static SchemaUtil schemaUtil = null;

    public static SchemaUtil getInstance() {
        if(schemaUtil == null) {
            schemaUtil = new SchemaUtil();
        }
        return schemaUtil;
    }


    public void createSchemaAndPopulateData(String userEmail) throws Exception {
        Long userID = OrgUtil.getInstance().getUserIDByEmail(userEmail);
        if(userID != null) {
            Map<String, Object> orgDetails = new HashMap<>();
            Long orgID = OrgUtil.getInstance().getOrgIDByUserID(userID);
            if(orgID != null) {
                orgDetails = OrgUtil.getInstance().getOrgDetailsByID(orgID);
                if(!orgDetails.isEmpty()) {
                    String orgName = (String) orgDetails.get("org_name");
                    orgName = orgName.replaceAll(" ", "");
                    String schemaName = createSchemaName();
                    try {
                        createSchema(schemaName);
                        setSearchPathForSchema(schemaName);
                        populateUserSpecificData();
                        setSearchPathToPublic();
                        updateSchemaDetails(orgID, schemaName);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Schema creation failed!, deleting prepopulated entry");
                        OrgUtil.getInstance().deletePrepopulatedDataForSchemaFailure(orgID, userID);
                        throw new Exception("schema creation failed");
                    }
                }
            }
        }

    }

    private void updateSchemaDetails(Long orgID, String schemaName) {
        if(orgID != null && schemaName != null && !schemaName.isEmpty()) {
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            int insertStatus = dslContext.insertInto(table("SASSchemaDetails")).columns(field("SCHEMA_NAME"), field("ORG_ID")).values(schemaName, orgID).execute();
            if(insertStatus > 0){
                logger.log(Level.INFO, "schema details mapping added successfully for orgID : {0} and SchemaName : {1}", new Object[]{orgID, schemaName});
            }
        } else {
            throw new NullPointerException("orgID or schemaName is null, unable to update details");
        }
    }

    private void setSearchPathToPublic() {
        String publicSchema = "public";
        setSearchPathForSchema(publicSchema);
    }

    private void setSearchPathForSchema(String schemaName) {
        if(schemaName != null && !schemaName.isEmpty()) {
            String sqlForSearchPath = "SET search_path TO " + schemaName;
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            dslContext.execute(sqlForSearchPath);
            logger.log(Level.INFO, "search path for sql statements updated successfully to : {0}", schemaName);
        } else {
            throw new NullPointerException("unable to set search_path, schema name is null");
        }
    }

    private void createSchema(String schemaName) {
        if(schemaName != null && !schemaName.isEmpty()) {
            String sqlForSchema = "CREATE SCHEMA IF NOT EXISTS " + schemaName;
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            dslContext.execute(sqlForSchema);
            logger.log(Level.INFO, "Schema with name : {0}, created successfully", schemaName);
        }
    }

    private void populateUserSpecificData() throws IOException {
        String jsonConfFilesPath = FileHandler.getHomeDir() + FileHandler.getFileSeparator() + "resources" + FileHandler.getFileSeparator() + "static-meta-specific.json";
        if(FileHandler.fileExists(jsonConfFilesPath)) {
            new StartUpService().populateStaticMetaDataFiles(jsonConfFilesPath);
            logger.log(Level.INFO, "Table data populated successfully");
        } else {
            logger.log(Level.WARNING, "static-meta-specific.json file doesn't exist ! , unable to populate static data");
            throw new FileNotFoundException("static-meta-specific.json file doesn't exist");
        }
    }

    private String createSchemaName() {
        Random random = new Random();
        char letter1 = (char) ('A' + random.nextInt(26));
        char letter2 = (char) ('A' + random.nextInt(26));

        int number1 = random.nextInt(10);
        int number2 = random.nextInt(10);
        int number3 = random.nextInt(10);
        int number4 = random.nextInt(10);

        return "db" + number1 + number2 + number3 + number4 + letter1 + letter2 ;
    }
}
