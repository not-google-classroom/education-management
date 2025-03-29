package com.org.education_management.factory;

import com.org.education_management.util.AppProperty;
import com.org.education_management.util.files.HDFSFileAccessImpl;
import com.org.education_management.util.files.LocalFileAccessImpl;

public class FactoryProvider {

    public static FileAccess getFileAccessImpl() {
        FileAccess fileAccess = null;
        if(AppProperty.getInstance().containsProperty("server.hadoop.enabled") && AppProperty.getInstance().getProperty("server.hadoop.enabled").equalsIgnoreCase("true")) {
            fileAccess = new HDFSFileAccessImpl();
        } else {
            fileAccess = new LocalFileAccessImpl();
        }
        return fileAccess;
    }
}
