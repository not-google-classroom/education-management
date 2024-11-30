package com.org.education_management.module.fees.service;

import com.org.education_management.database.DataBaseUtil;
import com.org.education_management.util.OrgUtil;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jooq.impl.DSL.*;
import static org.jooq.impl.DSL.name;

public class FeesService {
    private static final Logger logger = Logger.getLogger(FeesService.class.getName());

    public boolean createFeesStructure(JSONObject feesStructure) throws JSONException {
        feesStructure = getFeesData();
        if (!validateFeesKeys(feesStructure)) {
            return false;
        }

        Map<String, Object> insertData = new HashMap<>();
        insertData.put("INSTALLMENTS", feesStructure.getInt("noOfInstallments"));
        insertData.put("TOTAL_FEES", feesStructure.getLong("totalFees"));
        insertData.put("REMAINING_FEES", feesStructure.getLong("totalFees"));
        insertData.put("FEES_NAME", feesStructure.getString("feesName"));
        DataBaseUtil.insertData("fees", insertData);

        JSONArray installmentsArray = feesStructure.getJSONArray("installments");
        for (int i = 0; i < feesStructure.getInt("noOfInstallments"); i++) {
            JSONObject installmentJson = installmentsArray.getJSONObject(i);
            if (!validateInstallmentKeys(installmentJson)) {
                return false;
            }

            insertData = new HashMap<>();
            insertData.put("INSTALLMENT_AMOUNT", installmentJson.getLong("amount"));
            insertData.put("DUE_DATE", installmentJson.getLong("date"));
            insertData.put("INSTALLMENT_NAME", installmentJson.getString("installmentName"));
            DataBaseUtil.insertData("Installments", insertData);
        }
        return true;
    }

    public boolean payFees(Map<String, Object> requestMap) throws Exception {

        if (!requestMap.containsKey("userId") || requestMap.get("userId") == null) {
            return false;
        }
        if (!requestMap.containsKey("installmentId") || requestMap.get("installmentId") == null) {
            return false;
        }
        if (!requestMap.containsKey("transactionAmount") || requestMap.get("transactionAmount") == null) {
            return false;
        }

        Long userId = (Long) requestMap.get("userId");
        Long installmentId = (Long) requestMap.get("installmentId");
        Long transactionAmount = (Long) requestMap.get("transactionAmount");

        if (!OrgUtil.getInstance().checkIfUserExists(userId)) {
            return false;
        }

        if (!checkIfValidInstallementId(installmentId)) {
            return false;
        }

        Long feesId = getFeesIdFromInstallmentId(installmentId);
        if (feesId == null) {
            return false;
        }

        // Pay fees
        if (!addEntryInTransaction(installmentId, transactionAmount)) {
            return false;
        }

        return false;
    }

    public boolean mapFees(JSONObject feesStructure) throws Exception {

        if (!feesStructure.has("feesId")) {
            return false;
        }
        if (!feesStructure.has("userIds")) {
            return false;
        }

        Long feesId = feesStructure.getLong("feesId");
        JSONArray usersArray = feesStructure.getJSONArray("userIds");

        return false;
    }

    private boolean addEntryInTransaction(Long installmentId, Long transactionAmount) throws Exception {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        int record = dslContext.insertInto(table("Transactions")).columns(field("INSTALLMENT_ID"), field("TRANSACTION_AMOUNT"), field("TRANSACTION_DATE")).values(transactionAmount, installmentId, System.currentTimeMillis()).execute();
        if (record > 0) {
            logger.log(Level.INFO, "Transaction added successfully...");
        } else {
            logger.log(Level.SEVERE, "Transaction addition failed");
            throw new Exception("Exception when executing insert for Transactions");
        }
        return false;
    }

    private Long getFeesIdFromInstallmentId(Long installmentId) {
        if (installmentId != null) {
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            Record record = dslContext.select().from(table("FEES")).where(field("INSTALLMENT_ID").eq(installmentId)).fetchOne();
            if (record != null && record.size() > 0) {
                return (Long) record.get("FEES_ID");
            }
            //logger.log(Level.WARNING, "user details not found! to find schema details");
        }
        //logger.log(Level.WARNING, "unable to fetch schema Name for userID : {0}", installmentId);
        return null;
    }

    private boolean checkIfValidInstallementId(Long installmentId) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Record record = dslContext.select().from("Installments").where(field("INSTALLMENT_ID").eq(installmentId)).fetchOne();
        if (record != null) {
            return true;
        }
        return false;
    }

    private boolean validateFeesKeys(JSONObject feesStructure) {
        if (!feesStructure.has("noOfInstallments")) {
            return false;
        }
        if (!feesStructure.has("totalFees")) {
            return false;
        }
        if (!feesStructure.has("feesName")) {
            return false;
        }
        return true;
    }

    private boolean validateInstallmentKeys(JSONObject installmentJson) {
        if (!installmentJson.has("amount")) {
            return false;
        }
        if (!installmentJson.has("date")) {
            return false;
        }
        if (!installmentJson.has("installmentName")) {
            return false;
        }
        return true;
    }

    private JSONObject getFeesData() throws JSONException {
        String sf = "{\n" +
                "  \"feesName\": \"School Fees\",\n" +
                "  \"noOfInstallments\": 3,\n" +
                "  \"totalFees\": 60000,\n" +
                "  \"installments\": [\n" +
                "    {\n" +
                "      \"installmentName\": \"Quarterly\",\n" +
                "      \"amount\": \"20000\",\n" +
                "      \"date\": 1353463434363\n" +
                "    },\n" +
                "    {\n" +
                "      \"installmentName\": \"Halfearly\",\n" +
                "      \"amount\": \"20000\",\n" +
                "      \"date\": 1353463434363\n" +
                "    },\n" +
                "    {\n" +
                "      \"installmentName\": \"Annual\",\n" +
                "      \"amount\": \"20000\",\n" +
                "      \"date\": 1353463434363\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";

        return new JSONObject(sf);
    }
}
