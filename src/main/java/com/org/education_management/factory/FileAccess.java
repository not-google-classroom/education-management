package com.org.education_management.factory;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface FileAccess {
    boolean createDirectory(String filePath) throws Exception;
    boolean saveFile(MultipartFile fileDetails, String filePath, String fileName, String fileExtn) throws Exception;
    boolean deleteFile(Long fileID, String filePath) throws Exception;
    boolean deleteFile(String filePath) throws Exception;
    File getFile(String filePath, String checkSum) throws Exception;
}
