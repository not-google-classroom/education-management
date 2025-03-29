package com.org.education_management.service;

import com.org.education_management.database.DataBaseUtil;
import com.org.education_management.factory.FactoryProvider;
import com.org.education_management.util.AppProperty;
import com.org.education_management.util.files.FileHandler;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class FilesService {

    private static final Logger logger = Logger.getLogger(FilesService.class.getName());

    public Long uploadFileToTempLocation(Map<String, Object> requestBody) throws Exception {
        if(requestBody != null && !requestBody.isEmpty()) {
            try {
                String tempPath = FileHandler.getHomeDir() + FileHandler.getFileSeparator() + "temp" + FileHandler.getFileSeparator();
                MultipartFile multipartFile = (MultipartFile) requestBody.get("fileMeta");
                String fullFileName = multipartFile.getOriginalFilename();
                String fileNameWithOutExtn = fullFileName.substring(0, fullFileName.lastIndexOf("."));
                String fileName = fileNameWithOutExtn + "_" + System.currentTimeMillis();
                String fileExtn = fullFileName.substring(fullFileName.lastIndexOf("."));
                String checkSum = FileHandler.calculateCheckSum(multipartFile.getInputStream());
                if(FactoryProvider.getFileAccessImpl().saveFile(multipartFile, tempPath, fileName, fileExtn)) {
                    return updateFileDetailsInDB(multipartFile, tempPath, fileName, fileExtn, checkSum);
                } else {
                    logger.log(Level.WARNING, "unable to save the file in the desired location");
                    return -1L;
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "File meta couldn't be found, unable to upload file : {0}", e);
                throw new Exception("File cannot be uploaded! retry uploading");
            }
        }
        return -1L;
    }

    private Long updateFileDetailsInDB(MultipartFile multipartFile, String tempPath, String fileName, String fileExtn, String checkSum) {
        Long fileID = null;
        if(multipartFile != null) {
            String filePath = tempPath + fileName + fileExtn;
            boolean hdfsEnabled = AppProperty.getInstance().containsProperty("server.hadoop.enabled") && AppProperty.getInstance().getProperty("server.hadoop.enabled").equalsIgnoreCase("true");
            String storageType = hdfsEnabled ? "HDFS" : "LocalFS";
            DSLContext dslContext = DataBaseUtil.getDSLContext();
            Long uploadedTime = System.currentTimeMillis();
            Record record = dslContext.insertInto(table("TempFilesDetails"),
                            field("FILE_NAME"), field("FILE_EXTN"), field("FILE_PATH"), field("FILE_CHECKSUM"), field("STORAGE_TYPE"), field("UPLOADED_TIME"))
                    .values(fileName, fileExtn, filePath, checkSum, storageType, uploadedTime)
                    .returningResult(field("FILE_ID")) // Specify the auto-increment column
                    .fetchOne();
            if(record != null) {
                fileID = (Long) record.get(field("FILE_ID"));
                return fileID;
            }
        }
        logger.log(Level.WARNING, "File meta data is null!, unable store details");
        return fileID;
    }

    public File getUploadedFile(Long fileID) throws Exception {
        if(fileID != null && fileID != -1L) {
            Map<String, Object> fileDetails = getTempFileDetails(fileID);
            if(fileDetails != null && !fileDetails.isEmpty()) {
                String filePath = (String) fileDetails.get("file_path");
                String checkSum = (String) fileDetails.get("file_checksum");
                return FactoryProvider.getFileAccessImpl().getFile(filePath, checkSum);
            }
            logger.log(Level.WARNING, "File details is empty ! unable to find file with fileID : {0}", fileID);
            throw new FileNotFoundException("File with fileID : " + fileID + "couldn't be found in the database");
        }
        return null;
    }

    private Map<String, Object> getTempFileDetails(Long fileID) {
        Map<String, Object> resultMap = new HashMap<>();
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Record record = dslContext.select().from(table("TempFilesDetails")).where(field("FILE_ID").eq(fileID)).fetchOne();
        if(record != null) {
            resultMap = record.intoMap();
        }
        return resultMap;
    }

    public Map<Long, Object> getAllFiles() {
        Map<Long, Object> resultMap = new HashMap<>();
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Result<Record> result = dslContext.select().from(table("TempFilesDetails")).fetch();
        for(Record record : result) {
            resultMap.put((Long) record.get("file_id"), record.intoMap());
        }
        return resultMap;
    }

    public Map<Long, Object> getAllFilesOfThreshold(Long thresholdTime) {
        Map<Long, Object> resultMap = new HashMap<>();
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        Long actualThresholdTime = System.currentTimeMillis() * thresholdTime;
        Result<Record> result = dslContext.select().from(table("TempFilesDetails")).where(field("uploaded_time").greaterOrEqual(actualThresholdTime)).fetch();
        for(Record record : result) {
            resultMap.put((Long) record.get("file_id"), record.intoMap());
        }
        return resultMap;
    }

    public void bulkDeleteFiles(ArrayList<Long> deletedFileIDs) {
        DSLContext dslContext = DataBaseUtil.getDSLContext();
        if(deletedFileIDs != null && !deletedFileIDs.isEmpty()) {
            int delStatus = dslContext.deleteFrom(table("TempFileDetails")).where(field("file_id").in(deletedFileIDs)).execute();
            logger.log(Level.INFO, "bulkDeleteFiles status : {0}", delStatus);
        }
    }
}

