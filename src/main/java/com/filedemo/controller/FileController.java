package com.filedemo.controller;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.filedemo.service.FileStorageService;

/**
 * @author Ramesh naidu 
 *
 */
@RestController
public class FileController {


    @Autowired
    private FileStorageService fileStorageService;
    
    @Value("${file.upload-dir}")
    private String fileUploadDir;

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);
        return "Document uploaded successfuly";
    }
    


    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    
    
    @GetMapping("/getFiles")
    public ResponseEntity<List<String>> getAllTheFileInDir() throws IOException {
    	List<String> files = new ArrayList<>();
    	try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(fileUploadDir))) {
            for (Path path : stream) {
                //if (!Files.isDirectory(path)) {
                	files.add(path.getFileName()
                        .toString());
                //}
            }
        }
    	return new  ResponseEntity<>(files, HttpStatus.OK);
    }
    

}
