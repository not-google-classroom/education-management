package com.org.education_management.util;

import com.org.education_management.util.files.FileHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AppProperty {

    private final String propertiesFilePath;
    private Map<String, Map<String, String>> propertiesMap;
    private static AppProperty appProperty = null;

    // Constructor to initialize the utility with the properties file path
    public AppProperty() {
        this.propertiesFilePath = FileHandler.getHomeDir() + FileHandler.getFileSeparator() + "resources" + FileHandler.getFileSeparator() + "application.properties";
        loadProperties();
    }

    public static AppProperty getInstance() {
        if (appProperty == null) {
            appProperty = new AppProperty();
        }
        return appProperty;
    }

    // Load properties from the file
    private void loadProperties() {
        try {
            this.propertiesMap = FileHandler.readPropertiesFile(propertiesFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            this.propertiesMap = new HashMap<>();
        }
    }

    // Get a property value from the global section
    public String getProperty(String key) {
        return getProperty("global", key);
    }

    // Get a property value from a specific section
    public String getProperty(String section, String key) {
        Map<String, String> sectionProperties = propertiesMap.get(section);
        if (sectionProperties != null) {
            return sectionProperties.get(key);
        }
        return null;
    }

    // Set a property value in the global section
    public void setProperty(String key, String value) {
        setProperty("global", key, value);
    }

    // Set a property value in a specific section
    public void setProperty(String section, String key, String value) {
        Map<String, String> sectionProperties = propertiesMap.computeIfAbsent(section, k -> new HashMap<>());
        sectionProperties.put(key, value);
    }

    // Save the properties to the file
    public void saveProperties() {
        try {
            FileHandler.writePropertiesFile(propertiesFilePath, propertiesMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Reload the properties from the file
    public void reloadProperties() {
        loadProperties();
    }

    // Check if a property exists in the global section
    public boolean containsProperty(String key) {
        return containsProperty("global", key);
    }

    // Check if a property exists in a specific section
    public boolean containsProperty(String section, String key) {
        Map<String, String> sectionProperties = propertiesMap.get(section);
        return sectionProperties != null && sectionProperties.containsKey(key);
    }

    // Remove a property from the global section
    public void removeProperty(String key) {
        removeProperty("global", key);
    }

    // Remove a property from a specific section
    public void removeProperty(String section, String key) {
        Map<String, String> sectionProperties = propertiesMap.get(section);
        if (sectionProperties != null) {
            sectionProperties.remove(key);
        }
    }

    // Get all properties in the global section
    public Map<String, String> getAllProperties() {
        return getAllProperties("global");
    }

    // Get all properties in a specific section
    public Map<String, String> getAllProperties(String section) {
        return propertiesMap.getOrDefault(section, new HashMap<>());
    }

}
