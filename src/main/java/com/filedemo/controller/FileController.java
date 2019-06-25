package com.filedemo.controller;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
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

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.users.FullAccount;
import com.filedemo.service.FileStorageService;

/**
 * @author Ramesh naidu 
 *
 */
@RestController
public class FileController {

	
	@Value("${dropBox.file.dir}")
	private String dropBoxFolderPath;

	@Value("${file.upload-dir}")
	private String fileUploadDir;
	
	@Value("${dropBox.access_token}")
	private  String accessToken;

	    private DbxRequestConfig config = null;
	    DbxClientV2 client = null;
	    FullAccount account = null;
	    
	
	    @PostConstruct
	    public void initApplication() {
	    	 this.config = new DbxRequestConfig("file_upload_demo_proj");
	    	 this.client = new DbxClientV2(config, accessToken);
	    }
	    
	    
	
    @Autowired
    private FileStorageService fileStorageService;
    
   

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestParam("file") MultipartFile file) throws UploadErrorException, DbxException, IOException {
        //String fileName = fileStorageService.storeFile(file);
    	 //InputStream in = new FileInputStream(dbUploadFolder);
         FileMetadata metadata = client.files().uploadBuilder(dropBoxFolderPath+"/"+file.getOriginalFilename()).uploadAndFinish(file.getInputStream());
        return "File : " + file.getOriginalFilename()+" Document uploaded successfuly";
    }
    


    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) throws DbxException {
        String contentType = null;
        Resource resource = null;
        try {
        	  FileOutputStream downloadFile = new FileOutputStream(fileUploadDir + "/" + fileName);
        	  FileMetadata metadata = client.files().downloadBuilder(dropBoxFolderPath+"/"+fileName).download(downloadFile);
        	  resource = fileStorageService.loadFileAsResource(metadata.getName());
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
    public ResponseEntity<List<String>> getAllTheFileInDir() throws IOException, ListFolderErrorException, DbxException {
    	List<String> files = new ArrayList<>();
    	 ListFolderResult result = client.files().listFolder(dropBoxFolderPath);
             for (Metadata metadata : result.getEntries()) {
                 System.out.println(metadata.getName());
                 files.add(metadata.getName());
             }
    	
    	return new  ResponseEntity<>(files, HttpStatus.OK);
    }
    

}
