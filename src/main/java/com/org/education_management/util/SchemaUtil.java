package com.org.education_management.util;

import com.org.education_management.database.DataBaseUtil;
import com.org.education_management.service.StartUpService;
import com.org.education_management.util.files.FileHandler;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jooq.impl.DSL.*;

public class SchemaUtil {
    private static final Logger logger = Logger.getLogger(SchemaUtil.class.getName());
    public static SchemaUtil schemaUtil = null;

    public static SchemaUtil getInstance() {
        if(schemaUtil == null) {
            schemaUtil = new SchemaUtil();
        }
        return schemaUtil;
    }


    public String createSchemaAndPopulateData(String userEmail) throws Exception {
        Long userID = OrgUtil.getInstance().getUserIDByEmail(userEmail);
        if(userID != null) {
            Map<Long, Object> orgDetails = new HashMap<>();
            Long orgID = OrgUtil.getInstance().getOrgIDByUserID(userID);
            if(orgID != null) {
                orgDetails = OrgUtil.getInstance().getOrgDetailsByID(orgID);
                if(!orgDetails.isEmpty()) {
                    Map<Long, Object> orgDetailsMap = (Map<Long, Object>) orgDetails.get(orgID);
                    String orgName = (String) orgDetailsMap.get("org_name");
                    orgName = orgName.replaceAll(" ", "");
                    String schemaName = createSchemaName();
                    try {
                        createSchema(schemaName);
                        setSearchPathForSchema(schemaName);
                        populateUserSpecificData();

                        //Start default scheduler for the user
                        DynamicSchedulerUtil schedulerUtil = new DynamicSchedulerUtil();
                        schedulerUtil.loadDefaultSchedulersFromDatabase(orgID, schemaName);

                        //Do not update anything here , it is specific to Public DB
                        setSearchPathToPublic();
                        updateSchemaDetails(orgID, schemaName);
                        return schemaName;
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Schema creation failed!, deleting prepopulated entry : {0}", e);
                        OrgUtil.getInstance().deletePrepopulatedDataForSchemaFailure(orgID, userID);
                        throw new Exception("schema creation failed");
                    }
                }
            }
        }

        return null;
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

    public void setSearchPathToPublic() {
        String publicSchema = "public";
        setSearchPathForSchema(publicSchema);
    }

    public String getSearchPatch() {
        return DataBaseUtil.getDSLContext()
                .select(field("current_setting('search_path')", String.class))
                .fetchOneInto(String.class);
    }

    public void setSearchPathForSchema(String schemaName) {
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

    private void populateUserSpecificData() throws Exception {
        String jsonConfTablePath = FileHandler.getHomeDir() + FileHandler.getFileSeparator() + "resources" + FileHandler.getFileSeparator() + "static-table-specific.json";
        String jsonConfMetaPath = FileHandler.getHomeDir() + FileHandler.getFileSeparator() + "resources" + FileHandler.getFileSeparator() + "static-meta-specific.json";
        if(FileHandler.fileExists(jsonConfTablePath)) {
            StartUpService.getInstance().populateStaticTableDataFiles(jsonConfTablePath);
            logger.log(Level.INFO, "Table data populated successfully");
            if(FileHandler.fileExists(jsonConfMetaPath)) {
                StartUpService.getInstance().populateStaticMetaDataFiles(jsonConfMetaPath);
                logger.log(Level.INFO, "Meta data for tables populated successfully");
            }
        } else {
            logger.log(Level.WARNING, "static-table-specific.json file doesn't exist ! , unable to populate static data");
            throw new FileNotFoundException("static-table-specific.json file doesn't exist");
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

    public TreeMap<String, Object> getAllSchemas() {
        TreeMap<String, Object> allSchemas = new TreeMap<>();
        try {
            setSearchPathToPublic();
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            Result<Record> result = dslContext.selectFrom("sasschemadetails").fetch();
            if(!result.isEmpty()) {
                for(Record record : result) {
                    allSchemas.put(record.get(field(name("sasschemadetails", "schema_name"))).toString(),record.intoMap());
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when fetching all schema details");
        }
        return allSchemas;
    }

    public void startUserSpecificSchedulers() {
        try {
            setSearchPathToPublic();
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            Result<Record> result = dslContext.selectFrom("sasschemadetails").fetch();
            if(!result.isEmpty()) {
                for(Record record : result) {
                    String schemaName = record.get(field(name("sasschemadetails", "schema_name"))).toString();
                    Long orgID = (Long) record.get(field(name("sasschemadetails", "org_id")));
                    setSearchPathForSchema(schemaName);
                    //Start default scheduler for the user
                    DynamicSchedulerUtil schedulerUtil = new DynamicSchedulerUtil();
                    schedulerUtil.loadDefaultSchedulersFromDatabase(orgID, schemaName);
                    logger.log(Level.INFO, "Schedulers started for user schema : {0}", schemaName);
                    setSearchPathToPublic();
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception when fetching all schema details");
        } finally {
            setSearchPathToPublic();
        }
    }
}
