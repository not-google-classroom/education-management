package com.org.education_management.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.education_management.model.ApiRateLimit;
import com.org.education_management.model.TableMetaData;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Get the file separator used by the operating system
    public static String getFileSeparator() {
        return File.separator;
    }

    // Check if a file exists
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    // Remove a file
    public static boolean removeFile(String filePath) {
        try {
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Read a text file and return its content as a String
    public static String readTextFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    // Write content to a text file
    public static void writeTextFile(String filePath, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Append content to a text file
    public static void appendTextFile(String filePath, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Read a text file line by line into a List
    public static List<String> readTextFileLines(String filePath) {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    // Read a JSON file and map it to a Java object
    // Method to parse the JSON file and return a JsonNode
    public static JsonNode readJsonFile(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(); // Create an ObjectMapper instance
        return objectMapper.readTree(new File(filePath)); // Parse the file and return JsonNode
    }

    public static TableMetaData readSchemaFromFile(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), TableMetaData.class);
    }

    public static <T> T readJsonFile(String filePath, Class<T> tClass) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(); // Create an ObjectMapper instance
        return objectMapper.readValue(new File(filePath), tClass); // Parse the file and return the object of type T
    }

    // Read a JSON file and return a JsonNode
    public static JsonNode readJsonNode(String filePath) throws IOException {
        return objectMapper.readTree(new File(filePath)); // Parse the file and return JsonNode
    }

    public static String getHomeDir() {
        return System.getProperty("user.dir") + getFileSeparator() + "src" + getFileSeparator() + "main";
    }

    // Write a Java object to a JSON file
    public static <T> void writeJsonFile(String filePath, T object) {
        try {
            objectMapper.writeValue(new File(filePath), object);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get the size of a file in bytes
    public static long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // Rename a file
    public static boolean renameFile(String oldFilePath, String newFilePath) {
        try {
            Files.move(Paths.get(oldFilePath), Paths.get(newFilePath), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Copy a file
    public static boolean copyFile(String sourceFilePath, String destinationFilePath) {
        try {
            Files.copy(Paths.get(sourceFilePath), Paths.get(destinationFilePath), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Move a file
    public static boolean moveFile(String sourceFilePath, String destinationFilePath) {
        try {
            Files.move(Paths.get(sourceFilePath), Paths.get(destinationFilePath), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get the file extension
    public static String getFileExtension(String filePath) {
        String fileName = new File(filePath).getName();
        int lastIndex = fileName.lastIndexOf('.');
        return (lastIndex == -1) ? "" : fileName.substring(lastIndex + 1);
    }

    // Properties File Handling (including sectioned properties)
    public static Map<String, Map<String, String>> readPropertiesFile(String filePath) throws IOException {
        Map<String, Map<String, String>> propertiesMap = new HashMap<>();
        Map<String, String> currentSection = new HashMap<>();
        String currentSectionName = "global";

        propertiesMap.put(currentSectionName, currentSection);

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) {
                    continue;
                }

                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSectionName = line.substring(1, line.length() - 1).trim();
                    currentSection = propertiesMap.computeIfAbsent(currentSectionName, k -> new HashMap<>());
                    continue;
                }

                String[] keyValue = line.split("=", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    currentSection.put(key, value);
                }
            }
        }
        return propertiesMap;
    }

    public static void writePropertiesFile(String filePath, Map<String, Map<String, String>> propertiesMap) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<String, Map<String, String>> sectionEntry : propertiesMap.entrySet()) {
                String sectionName = sectionEntry.getKey();
                Map<String, String> sectionProperties = sectionEntry.getValue();

                if (!"global".equals(sectionName)) {
                    writer.write("[" + sectionName + "]");
                    writer.newLine();
                }

                for (Map.Entry<String, String> property : sectionProperties.entrySet()) {
                    writer.write(property.getKey() + "=" + property.getValue());
                    writer.newLine();
                }
                writer.newLine();
            }
        }
    }

    public static HashMap<String, String> readPropsFile(String filePath) throws IOException {
        HashMap<String, String> propsMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                String[] keyValue = line.split("=", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    propsMap.put(key, value);
                }
            }
        }
        return propsMap;
    }

    public static Map<String, ApiRateLimit> readSecurityJsonFile(String securityJson) throws Exception{
        String jsonContent = new String(Files.readAllBytes(Paths.get(securityJson)));
        JSONObject jsonObject = new JSONObject(jsonContent);

        // Convert JSON to Map using streams
        return jsonObject.keySet().stream()
                .collect(Collectors.toMap(
                        key -> key, // Key is the API path
                        key -> {
                            JSONObject ruleObject = jsonObject.getJSONObject(key);
                            int limit = ruleObject.getInt("limit");
                            int window = ruleObject.getInt("window");
                            int lock = ruleObject.getInt("lockPeriod");
                            return new ApiRateLimit(limit, window, lock);
                        }
                ));
    }
}