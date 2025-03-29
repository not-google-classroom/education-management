package com.org.education_management.util.files;

import com.org.education_management.factory.FileAccess;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.FileSystemException;

public class LocalFileAccessImpl implements FileAccess {

    @Override
    public boolean createDirectory(String path) throws Exception {
        boolean isDirCreated = false;
        if(path != null && !path.isEmpty()) {
            FileHandler.createDirectoryIfNotExists(path);
            isDirCreated = true;
        }
        return isDirCreated;
    }

    @Override
    public boolean saveFile(MultipartFile fileDetails, String filePath, String fileName, String fileExtn) throws Exception {
        boolean isFileSaved = false;
        if(fileDetails != null && filePath !=null) {
            if(createDirectory(filePath)) {
                fileDetails.transferTo(new File(filePath + FileHandler.getFileSeparator() + fileName + fileExtn));
                isFileSaved = true;
            }
        }
        return isFileSaved;
    }

    @Override
    public boolean deleteFile(Long fileID, String filePath) throws Exception {
        return false;
    }

    @Override
    public boolean deleteFile(String filePath) throws Exception {
        if(filePath != null && !filePath.isEmpty()) {
            if(FileHandler.fileExists(filePath)) {
                return FileHandler.removeFile(filePath);
            } else {
                throw new FileNotFoundException("the requested file : " + filePath + " does not exists");
            }
        }
        return false;
    }

    @Override
    public File getFile(String filePath, String checkSum) throws Exception {
        if(FileHandler.fileExists(filePath)) {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            String fileChecksum = FileHandler.calculateCheckSum(fis);
            if(checkSum.equals(fileChecksum)) {
                return file;
            }
            throw new FileSystemException("The Requested file : " + filePath + " , has incorrect checksum");
        } else {
            throw new FileNotFoundException("Requested file : " + filePath + " cannot be found in this location");
        }
    }
}
