package com.org.education_management.controller;

import com.org.education_management.service.FilesService;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URLConnection;
import java.nio.file.FileSystemException;
import java.util.Map;

import static com.org.education_management.module.fees.controller.ResponseEntityWrapper.buildResponse;

@RestController
@RequestMapping("api/files")
public class FilesController {

    @Autowired
    HttpServletResponse response;

    FilesService filesService = new FilesService();

    @PostMapping(value = "/uploadFile")
    private ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam Map<String, Object> requestBody) {
        try {
            if (requestBody != null) {
                requestBody.put("fileMeta", file);
                Long fileID = filesService.uploadFileToTempLocation(requestBody);
                return buildResponse(HttpStatus.OK, "file uploaded to server successfully", new JSONObject().put("fileID", fileID));
            } else {
                return buildResponse(HttpStatus.BAD_REQUEST, "Input file is empty unable to process request");
            }
        } catch (Exception e) {
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "unable to process request! contact support");
        }
    }

    @GetMapping(value = "/getFile")
    private ResponseEntity<Map<String, Object>> getFile(@RequestParam("fileID") Long fileID) {
        if(fileID != null && fileID != -1L) {
            try {
                File file = filesService.getUploadedFile(fileID);
                FileInputStream fis = new FileInputStream(file);
                URLConnection connection = file.toURL().openConnection();
                String mimeType = connection.getContentType();
                byte[] content = fis.readAllBytes();
                response.reset();
                response.setContentType(mimeType);
                response.setHeader("Content-Disposition", "attachment;        filename="+file.getName());
                BufferedOutputStream outStream = new BufferedOutputStream(response.getOutputStream());
                outStream.write(content);
                outStream.close();
                return buildResponse(HttpStatus.OK, "file retrieved successfully");
            } catch (FileNotFoundException fe) {
                return buildResponse(HttpStatus.BAD_REQUEST, "requested file not found!");
            }catch (FileSystemException fse) {
                return buildResponse(HttpStatus.FORBIDDEN, "uploaded file is tampered! checksum mismatch");
            } catch (Exception e) {
                return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "unable to process request! contact support");
            }
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "unable to process request! contact support");
    }
}
