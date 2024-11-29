package com.org.education_management.module.fees.service;

import com.org.education_management.database.DataBaseUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FeesService {
    public boolean createFeesStructure(JSONObject feesStructure) throws JSONException {
        if (!validateFeesKeys(feesStructure)) {
            return false;
        }

        Map<String, Object> insertData = new HashMap<>();
        insertData.put("INSTALLMENTS", feesStructure.getInt("noOfInstallments"));
        insertData.put("TOTAL_FEES", feesStructure.getLong("totalFees"));
        insertData.put("REMAINING_FEES", feesStructure.getLong("totalFees"));
        insertData.put("FEES_NAME", feesStructure.getString("feesName"));
        DataBaseUtil.insertData("Fees", insertData);

        JSONArray installmentsArray = feesStructure.getJSONArray("installments");
        for (int i = 0; i < feesStructure.getInt("noOfInstallments"); i++) {
            JSONObject installmentJson = installmentsArray.getJSONObject(i);
            if (!validateInstallmentKeys(installmentJson)) {
                return false;
            }

            insertData = new HashMap<>();
            insertData.put("INSTALLMENT_AMOUNT", installmentJson.getLong("amount"));
            insertData.put("DUE_DATE", installmentJson.getLong("date"));
            insertData.put("INSTALLMENT_NAME", installmentJson.getString("feesName"));
            DataBaseUtil.insertData("Installments", insertData);
        }
        return true;
    }

    public boolean payFees(Long userId, Long installmentId) {
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
}
