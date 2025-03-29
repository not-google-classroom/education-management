package com.org.education_management.util.files;

import com.org.education_management.factory.FileAccess;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public class HDFSFileAccessImpl implements FileAccess {
    @Override
    public boolean createDirectory(String filePath) throws Exception {
        return false;
    }

    @Override
    public boolean saveFile(MultipartFile fileDetails, String filePath, String fileName, String fileExtn) throws Exception {
        return false;
    }

    @Override
    public boolean deleteFile(Long fileID, String filePath) throws Exception {
        return false;
    }

    @Override
    public boolean deleteFile(String filePath) throws Exception {
        return false;
    }

    @Override
    public File getFile(String filePath, String checkSum) throws Exception {
        return null;
    }
}
