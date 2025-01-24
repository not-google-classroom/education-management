package com.org.education_management.registry;

import java.util.prefs.Preferences;

public class RegistryManager {
    private static final String BASE_PATH = "Software\\EducationManagement";

    private static String resolveRegistryPath(String registryPath) {
        // If no path provided, use default
        if (registryPath == null || registryPath.trim().isEmpty()) {
            return BASE_PATH;
        }
        // Append provided path to base path
        return BASE_PATH + "\\" + registryPath;
    }

    public static String getRegistryString(String registryPath, String key) {
        try {
            String fullPath = resolveRegistryPath(registryPath);
            Preferences prefs = Preferences.userRoot().node(fullPath);
            return prefs.get(key, null);
        } catch (Exception e) {
            System.err.println("Error reading registry: " + e.getMessage());
            return null;
        }
    }

    public static String getRegistryString(String key) {
        return getRegistryString(null, key);
    }

    public static boolean setRegistryString(String registryPath, String key, String value) {
        try {
            String fullPath = resolveRegistryPath(registryPath);
            Preferences prefs = Preferences.userRoot().node(fullPath);
            prefs.put(key, value);
            prefs.flush();
            return true;
        } catch (Exception e) {
            System.err.println("Error writing to registry: " + e.getMessage());
            return false;
        }
    }

    public static boolean setRegistryString(String key, String value) {
        return setRegistryString(null, key, value);
    }

    public static boolean deleteRegistryKey(String registryPath, String key) {
        try {
            String fullPath = resolveRegistryPath(registryPath);
            Preferences prefs = Preferences.userRoot().node(fullPath);
            prefs.remove(key);
            prefs.flush();
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting registry key: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteRegistryKey(String key) {
        return deleteRegistryKey(null, key);
    }

    public static boolean hasRegistryKey(String registryPath, String key) {
        try {
            String fullPath = resolveRegistryPath(registryPath);
            Preferences prefs = Preferences.userRoot().node(fullPath);
            return prefs.get(key, null) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasRegistryKey(String key) {
        return hasRegistryKey(null, key);
    }
}