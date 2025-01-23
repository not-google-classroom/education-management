package com.org.education_management.module.fees.service;

import com.org.education_management.database.DataBaseUtil;
import com.org.education_management.util.OrgUtil;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jooq.impl.DSL.*;

public class FeesService {
    private static final Logger logger = Logger.getLogger(FeesService.class.getName());

    @Transactional
    public boolean createFeesStructure(JSONObject feesStructure) throws JSONException {
        if (!validateFeesKeys(feesStructure)) {
            return false;
        }

        Map<String, Object> insertData = new HashMap<>();
        insertData.put("INSTALLMENTS", feesStructure.getInt("noOfInstallments"));
        insertData.put("TOTAL_FEES", feesStructure.getLong("totalFee"));
        insertData.put("FEES_NAME", feesStructure.getString("feeName"));
        if (!DataBaseUtil.insertData("fees", insertData)) {
            return false;
        }

        List installmentsArray = (ArrayList) feesStructure.get("installments");
        for (int i = 0; i < feesStructure.getInt("noOfInstallments"); i++) {
            Map installmentJson = (Map) installmentsArray.get(i);
            if (!validateInstallmentKeys(installmentJson)) {
                return false;
            }

            insertData = new HashMap<>();
            insertData.put("INSTALLMENT_AMOUNT", Double.parseDouble((String) installmentJson.get("amount")));
            insertData.put("DUE_DATE", Long.parseLong((String) installmentJson.get("dueDate")));
            insertData.put("INSTALLMENT_NAME", installmentJson.get("name"));
            if (!DataBaseUtil.insertData("Installments", insertData)) {
                return false;
            }
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
        if (!addEntryInTransaction(feesId, installmentId, userId, transactionAmount)) {
            return false;
        }

        if (!updateBalanceAmount(userId, feesId, transactionAmount)) {
            return false;
        }

        return false;
    }

    private boolean updateBalanceAmount(Long userId, Long feesId, Long transactionAmount) throws Exception {
        JSONObject balanceFeesMap = getBalanceFeesForUser(userId, feesId);
        Long balanceFees = (Long) balanceFeesMap.get("balanceFees");
        if (balanceFees < transactionAmount) {
            return false;
        }

        DSLContext dslContext = DataBaseUtil.getDSLContext();
        dslContext.update(table("feesmapping")).set(field("BALANCE_FEES"), balanceFees - transactionAmount).where(field("USER_ID").eq(userId)).and(field("FEES_ID").eq(feesId)).execute();
        return true;
    }

    public boolean mapFees(JSONObject feesStructure) throws Exception {
        feesStructure = getMappingData();
        if (!feesStructure.has("feesId")) {
            return false;
        }
        if (!feesStructure.has("userIds")) {
            return false;
        }

        Long feesId = feesStructure.getLong("feesId");
        JSONArray usersArray = feesStructure.getJSONArray("userIds");

        for (int i = 0; i < usersArray.length(); i++) {
            Long userId = ((Integer) usersArray.get(i)).longValue();
            Map<String, Object> insertData = new HashMap<>();
            Long totalFees = getFeesAmount(feesId);
            insertData.put("USER_ID", userId);
            insertData.put("FEES_ID", feesId);
            insertData.put("TOTAL_FEES", totalFees);
            insertData.put("BALANCE_FEES", totalFees);
            if (!DataBaseUtil.insertData("feesmapping", insertData)) {
                return false;
            }
        }
        return true;
    }

    private Long getFeesAmount(Long feesId) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Record record = dslContext.select().from(table("FEES")).where(field("FEES_ID").eq(feesId)).fetchOne();
        if (record != null && record.size() > 0) {
            return (Long) record.get("TOTAL_FEES");
        }
        return 0L;
    }

    public JSONObject getAllFees() throws Exception {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        List<Map<String, Object>> feesArray = new ArrayList<>();
        Result<Record> result = dslContext.select().from("fees").fetch();

        for (Record record : result) {
            Map<String, Object> feesObject = new HashMap<>();
            feesObject.put("fees_name", record.get("fees_name", String.class));
            feesObject.put("total_fees", record.get("total_fees").toString());
            feesObject.put("installments", record.get("installments").toString());
            feesArray.add(feesObject);
        }

        JSONObject feesDetails = new JSONObject();
        feesDetails.put("feesDetails", feesArray);
        return feesDetails;
    }

    public JSONObject getFeesIdsForUser(Map<String, Object> requestMap) throws Exception {
        JSONObject feesId = new JSONObject();
        if (!requestMap.containsKey("userId") || requestMap.get("userId") == null) {
            return feesId;
        }

        Long userId = Long.parseLong(requestMap.get("userId").toString());
        if (!OrgUtil.getInstance().checkIfUserExists(userId)) {
            return feesId;
        }
        return feesId.put("fees", getFeesForUser(userId));
    }

    public JSONObject getBalanceFeesForUser(Map<String, Object> requestMap) throws Exception {
        JSONObject balanceFees = new JSONObject();
        if (!requestMap.containsKey("userId") || requestMap.get("userId") == null) {
            return balanceFees;
        }

        if (!requestMap.containsKey("feesId") || requestMap.get("feesId") == null) {
            return balanceFees;
        }

        Long userId = Long.parseLong(requestMap.get("userId").toString());
        Long feesId = Long.parseLong(requestMap.get("feesId").toString());
        if (!OrgUtil.getInstance().checkIfUserExists(userId)) {
            return balanceFees;
        }
        return getBalanceFeesForUser(userId, feesId);
    }

    private Long getFineAmount(Long userId, Long feesId) {
        Long fineAmount = 0L;
        Long fineId = getFineId(userId, feesId);
        if (fineId == 0L) {
            return fineAmount;
        }
        Map<String, String> fineDetailsMap = getFineDetails(fineId);
        Boolean oneTimeFine = Boolean.valueOf(fineDetailsMap.get("oneTimeFine"));
        if (oneTimeFine) {
            return Long.parseLong(fineDetailsMap.get("fineAmount"));
        } else {
            Long fineFrom = Long.parseLong(fineDetailsMap.get("fineFrom"));
            Long fineTill = Long.parseLong(fineDetailsMap.get("fineTill"));
            Long timeDiffInMs = fineTill - fineFrom;
            Long fineMultiplier = 0L;
            String fineEveryDuration = fineDetailsMap.get("fineEveryDuration");

            switch (fineEveryDuration) {
                case "DAYS":
                    fineMultiplier = timeDiffInMs / 86400000L;
                case "WEEKS":
                    fineMultiplier = timeDiffInMs / 604800000L;
                case "MONTHS":
                    fineMultiplier = timeDiffInMs / 2592000000L;
                default:
                    fineMultiplier = 0L;
            }
            return fineMultiplier * Long.parseLong(fineDetailsMap.get("fineAmount"));
        }
    }

    private Map<String, String> getFineDetails(Long fineId) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Map<String, String> fineDetailsMap = new HashMap<>();
        Record record = dslContext.select().from(table("fine")).where(field("FINE_ID").eq(fineId)).fetchOne();
        if (record != null && record.size() > 0) {
            fineDetailsMap.put("fineName", (String) record.get("FINE_NAME"));
            fineDetailsMap.put("fineAmount", (String) record.get("FINE_AMOUNT"));
            fineDetailsMap.put("oneTimeFine", (String) record.get("ONE_TIME_FINE"));
            fineDetailsMap.put("fineEveryDuration", (String) record.get("FINE_EVERY_DURATION"));
            fineDetailsMap.put("fineFrom", (String) record.get("FINE_FROM"));
            fineDetailsMap.put("fineTill", (String) record.get("FINE_TILL"));
        }
        return fineDetailsMap;
    }

    private Long getFineId(Long userId, Long feesId) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Long fineId = 0L;
        Record record = dslContext.select().from(table("finemapping")).where(field("FEES_ID").eq(feesId)).and(field("USER_ID").eq(userId)).fetchOne();
        if (record != null && record.size() > 0) {
            fineId = (Long) record.get("FINE_ID");
        } else {
            return fineId;
        }
        return fineId;
    }

    public JSONObject getBalanceFeesForUser(Long userId, Long feesId) throws Exception {
        JSONObject balanceFees = new JSONObject();

        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Record record = dslContext.select().from(table("feesmapping")).where(field("FEES_ID").eq(feesId)).and(field("USER_ID").eq(userId)).fetchOne();
        if (record != null && record.size() > 0) {
            balanceFees.put("balanceFees", (Long) record.get("BALANCE_FEES"));
            balanceFees.put("totalFees", (Long) record.get("TOTAL_FEES"));
            balanceFees.put("fineAmount", getFineAmount(userId, feesId));
        }
        return balanceFees;
    }

    public boolean createFine(Map<String, Object> requestMap) {
        Map<String, Object> insertData = new HashMap<>();
        // Mandatory keys
        if (!requestMap.containsKey("fineName") || requestMap.get("fineName") == null) {
            return false;
        }
        if (!requestMap.containsKey("fineAmount") || requestMap.get("fineAmount") == null) {
            return false;
        }
        if (!requestMap.containsKey("oneTimeFine") || requestMap.get("oneTimeFine") == null) {
            return false;
        }

        // Optional keys
        if (requestMap.containsKey("fineEveryDuration") || requestMap.get("fineEveryDuration") != null) {
            insertData.put("FINE_EVERY_DURATION", requestMap.get("fineEveryDuration"));
        }
        if (requestMap.containsKey("fineFrom") || requestMap.get("fineFrom") != null) {
            insertData.put("FINE_FROM", Long.parseLong((String) requestMap.get("fineFrom")));
        }
        if (requestMap.containsKey("fineTill") || requestMap.get("fineTill") != null) {
            insertData.put("FINE_TILL", Long.parseLong((String) requestMap.get("fineTill")));
        }

        insertData.put("FINE_NAME", requestMap.get("fineName"));
        insertData.put("FINE_AMOUNT", Long.parseLong((String) requestMap.get("fineAmount")));
        insertData.put("ONE_TIME_FINE", Boolean.getBoolean(requestMap.get("oneTimeFine").toString()));
        if (!DataBaseUtil.insertData("fine", insertData)) {
            return false;
        }
        return true;
    }

    public boolean mapFineToUsers(Map<String, Object> requestMap) throws JSONException {
        JSONObject fineDetails = new JSONObject(requestMap);
        if (!fineDetails.has("fineId")) {
            return false;
        }
        if (!fineDetails.has("feesId")) {
            return false;
        }
        if (!fineDetails.has("userIds")) {
            return false;
        }

        Long fineId = fineDetails.getLong("fineId");
        Long feesId = fineDetails.getLong("feesId");
        JSONArray usersArray = fineDetails.getJSONArray("userIds");

        for (int i = 0; i < usersArray.length(); i++) {
            Long userId = ((Integer) usersArray.get(i)).longValue();
            Map<String, Object> insertData = new HashMap<>();
            Long totalFees = getFeesAmount(fineId);
            insertData.put("USER_ID", userId);
            insertData.put("FEES_ID", fineId);
            insertData.put("FINE_ID", fineId);
            if (!DataBaseUtil.insertData("finemapping", insertData)) {
                return false;
            }
        }
        return true;
    }

    private JSONArray getFeesForUser(Long userId) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        JSONArray feesIdList = new JSONArray();
        Result<Record> result = dslContext.select().from("feesmapping").where(field("USER_ID").eq(userId)).fetch();
        for (Record record : result) {
            feesIdList.put((Long) record.get("fees_id"));
        }
        return feesIdList;
    }


    private JSONObject getMappingData() throws JSONException {
        return new JSONObject("{\n" + "    \"feesId\" : 1,\n" + "    \"userIds\" : [1]\n" + "}");
    }

    private boolean addEntryInTransaction(Long feesId, Long installmentId, Long userId, Long transactionAmount) throws Exception {
        Map<String, Object> insertData = new HashMap<>();
        insertData.put("FEES_ID", feesId);
        insertData.put("INSTALLMENT_ID", installmentId);
        insertData.put("USER_ID", userId);
        insertData.put("TRANSACTION_AMOUNT", transactionAmount);
        insertData.put("TRANSACTION_DATE", System.currentTimeMillis());
        if (!DataBaseUtil.insertData("Transactions", insertData)) {
            return false;
        }
        logger.log(Level.INFO, "Transaction added successfully...");
        return true;
    }

    private Long getFeesIdFromInstallmentId(Long installmentId) {
        if (installmentId != null) {
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            Record record = dslContext.select().from(table("FEES")).where(field("INSTALLMENT_ID").eq(installmentId)).fetchOne();
            if (record != null && record.size() > 0) {
                return (Long) record.get("FEES_ID");
            }
        }
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
        if (!feesStructure.has("totalFee")) {
            return false;
        }
        if (!feesStructure.has("feeName")) {
            return false;
        }
        return true;
    }

    private boolean validateInstallmentKeys(Map<String, Object> installmentJson) {
        if (!installmentJson.containsKey("amount")) {
            return false;
        }
        if (!installmentJson.containsKey("dueDate")) {
            return false;
        }
        if (!installmentJson.containsKey("name")) {
            return false;
        }
        return true;
    }

    private JSONObject getFeesData() throws JSONException {
        String sf = "{\n" + "  \"feesName\": \"School Fees\",\n" + "  \"noOfInstallments\": 3,\n" + "  \"totalFees\": 60000,\n" + "  \"installments\": [\n" + "    {\n" + "      \"installmentName\": \"Quarterly\",\n" + "      \"amount\": \"20000\",\n" + "      \"date\": 1353463434363\n" + "    },\n" + "    {\n" + "      \"installmentName\": \"Halfearly\",\n" + "      \"amount\": \"20000\",\n" + "      \"date\": 1353463434363\n" + "    },\n" + "    {\n" + "      \"installmentName\": \"Annual\",\n" + "      \"amount\": \"20000\",\n" + "      \"date\": 1353463434363\n" + "    }\n" + "  ]\n" + "}\n";

        return new JSONObject(sf);
    }
}
