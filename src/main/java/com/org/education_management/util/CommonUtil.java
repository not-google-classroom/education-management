package com.org.education_management.util;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommonUtil {
    private static HashMap<String, String> classPaths = null;
    private static CommonUtil commonUtil = null;
    private static final Logger logger = Logger.getLogger(CommonUtil.class.getName());

    public static CommonUtil getInstance(){
        if(commonUtil == null){
            commonUtil = new CommonUtil();
        }
        return commonUtil;
    }

    public Object getObjForClassName(String className){
        Object obj = null;
        try{
            String objPath = getPathForClass(className);
            if(objPath != null){
                obj = Class.forName(objPath).getDeclaredConstructor().newInstance();
            }
        }
        catch (Exception e){
            logger.log(Level.SEVERE, "Exception while getting object for class name", e);
        }
        return obj;
    }

    public String getPathForClass(String className) {
        String path = null;
        HashMap classPathMap = null;
        try {
            if(classPaths == null){
                String classPathFile = FileHandler.getHomeDir() + FileHandler.getFileSeparator() + "resources" + FileHandler.getFileSeparator() + "class-loader.properties";
                if(FileHandler.fileExists(classPathFile)){
                    classPathMap = FileHandler.readPropsFile(classPathFile);
                }
                else {
                    logger.log(Level.WARNING, "Props File not present");
                }
            }
            if(classPathMap != null && !classPathMap.isEmpty()){
                if(classPathMap.containsKey(className)){
                    path = (String) classPathMap.get(className);
                }
                else {
                    logger.log(Level.WARNING,"class path not present in props file");
                }
            }
        }
        catch (Exception e){
            logger.log(Level.SEVERE, "Exception while getting path for classname",e);
        }
        return path;
    }
}
