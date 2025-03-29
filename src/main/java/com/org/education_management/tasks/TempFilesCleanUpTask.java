package com.org.education_management.tasks;

import com.org.education_management.factory.FactoryProvider;
import com.org.education_management.service.FilesService;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TempFilesCleanUpTask {
    private static Logger logger = Logger.getLogger(TempFilesCleanUpTask.class.getName());
    public void execute() {
        logger.log(Level.INFO, "temporary files clean up task started.");
        Long thresholdTime = 10L;
        Map<Long, Object> fileDetails = new FilesService().getAllFilesOfThreshold(thresholdTime);
        ArrayList<Long> deletedFileIDs = new ArrayList<>();
        if(fileDetails != null && !fileDetails.isEmpty()) {
            for(Long fileID : fileDetails.keySet()) {
                Map<String, Object> file = (Map<String, Object>) fileDetails.get(fileID);
                String filePath = (String) file.get("file_path");
                try {
                    if (FactoryProvider.getFileAccessImpl().deleteFile(filePath)) {
                        logger.log(Level.INFO, "File with fileID : {0}, deleted successfully");
                        deletedFileIDs.add(fileID);
                    } else {
                        logger.log(Level.WARNING, "unable to delete the fileID : {0} , filePath : {1}", new Object[]{fileID, filePath});
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Exception when file clean up : {0}", e);
                }
            }
            new FilesService().bulkDeleteFiles(deletedFileIDs);
        }

    }
}
