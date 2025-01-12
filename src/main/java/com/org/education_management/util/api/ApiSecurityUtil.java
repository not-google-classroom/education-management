package com.org.education_management.util.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.org.education_management.model.ApiRule;
import com.org.education_management.model.ApiRulesWrapper;
import com.org.education_management.model.TemplateRule;
import com.org.education_management.util.FileHandler;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApiSecurityUtil {

    private static final Logger logger = Logger.getLogger(ApiSecurityUtil.class.getName());
    private static final List<ApiRule> apiRules = new LinkedList<>();
    private static final List<TemplateRule> templateRules = new LinkedList<>();
    private static ApiSecurityUtil apiSecurityUtil = null;

    // Singleton pattern to ensure only one instance of ApiSecurityUtil
    public static ApiSecurityUtil getInstance() {
        if (apiSecurityUtil == null) {
            apiSecurityUtil = new ApiSecurityUtil();
        }
        return apiSecurityUtil;
    }

    // Private constructor to restrict instantiation
    private ApiSecurityUtil() {
        String apiConfigFile = FileHandler.getHomeDir() + FileHandler.getFileSeparator() + "resources" + FileHandler.getFileSeparator() + "api-config.json";

        try {
            JsonNode metaDataNode = FileHandler.readJsonNode(apiConfigFile);
            if (metaDataNode == null) {
                logger.log(Level.SEVERE, "No data found in api-config.json file!");
                return;
            }

            Iterator<String> fieldNames = metaDataNode.fieldNames();
            while (fieldNames.hasNext()) {
                String moduleName = fieldNames.next();
                JsonNode moduleData = metaDataNode.get(moduleName);

                String fileName = moduleData.get("file_name").asText();
                String filePath = moduleData.get("file_path").asText();

                String fullFilePath = Paths.get(FileHandler.getHomeDir(), filePath).toString();

                // Read an array of ApiRule
                List<ApiRule> apiRuleList = FileHandler.readJsonFile(fullFilePath, ApiRulesWrapper.class).getUrls();
                List<TemplateRule> templateRulesList = new LinkedList<>();
                try {
                    templateRulesList = FileHandler.readJsonFile(fullFilePath, ApiRulesWrapper.class).getTemplates();
                } catch (Exception e) {
                    logger.log(Level.FINE, "No template Rule found!");
                }
                if (apiRuleList != null && !apiRuleList.isEmpty()) {
                    for (ApiRule apiRule : apiRuleList) {
                        apiRules.add(apiRule);
                        logger.log(Level.INFO, "Loaded API Rule: {0}", apiRule);
                    }
                } else {
                    logger.log(Level.WARNING, "No API Rules found in file: {0}", fullFilePath);
                }
                if (templateRulesList != null && !templateRulesList.isEmpty()) {
                    for (TemplateRule templateRule : templateRulesList) {
                        templateRules.add(templateRule);
                        logger.log(Level.INFO, "Loaded Template Rule: {0}", templateRule);
                    }
                } else {
                    logger.log(Level.WARNING, "No Template Rules found in file: {0}", fullFilePath);
                }
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to read data from api-config.json file!", e);
        }
    }

    public List<ApiRule> getApiRules() {
        logger.log(Level.INFO, "Retrieving API Rules: {0}", apiRules.size());
        return apiRules;
    }

    public List<TemplateRule> getTemplateRules() {
        logger.log(Level.INFO, "Retrieving Template Rules: {0}", templateRules.size());
        return templateRules;
    }
}