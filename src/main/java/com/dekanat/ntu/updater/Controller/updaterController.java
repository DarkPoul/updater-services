package com.dekanat.ntu.updater.Controller;

import com.dekanat.ntu.updater.Entity.VersionInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@RestController
public class updaterController {

    @GetMapping("/check-update")
    public String checkUpdate() throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        File file = new File("/app/version.json");
        VersionInfo versionInfo = mapper.readValue(file, VersionInfo.class);

        System.out.println(versionInfo.getVersion());

        return "redirect:/download";

    }

    @GetMapping("/download-update")
    public ResponseEntity<Resource> download() throws IOException {
        Path filePath = Paths.get("/app/Dekanat.exe");
        return getResourceResponseEntity(filePath);
    }

    @GetMapping("/download-installer")
    public ResponseEntity<Resource> downloadInstaller() throws IOException {
        Path filePath = Paths.get("/app/DekanatSetup.exe.exe");
        return getResourceResponseEntity(filePath);
    }

    private ResponseEntity<Resource> getResourceResponseEntity(Path filePath) throws IOException {
        if (Files.exists(filePath)) {
            Resource file = new InputStreamResource(Files.newInputStream(filePath));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Dekanat.exe");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(file);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    @PostMapping("/upload-update")
    public ResponseEntity<Void> uploadUpdate(
            @RequestParam("jsonFile") MultipartFile jsonFile,
            @RequestParam("exeFile") MultipartFile exeFile,
            @RequestParam("exeFileInstaller") MultipartFile exeFileInstaller ){

        try {
            // Обробка завантаженого JSON файлу
            Path jsonFilePath = Paths.get("/app/" + jsonFile.getOriginalFilename());
            Files.createDirectories(jsonFilePath.getParent());
            Files.write(jsonFilePath, jsonFile.getBytes());

            // Обробка завантаженого .exe файлу
            uploadFile(exeFile);
            uploadFile(exeFileInstaller);
        } catch(IOException ex) {
            ex.fillInStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/download"));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    private void uploadFile(@RequestParam("exeFile") MultipartFile exeFileInstaller) throws IOException {
        if (exeFileInstaller != null && !exeFileInstaller.isEmpty() &&
                (Objects.equals(exeFileInstaller.getContentType(), "application/octet-stream") ||
                        Objects.equals(exeFileInstaller.getContentType(), "application/x-msdownload"))) {
            Path exeFilePath = Paths.get("/app/" + exeFileInstaller.getOriginalFilename());
            Files.createDirectories(exeFilePath.getParent());
            Files.write(exeFilePath, exeFileInstaller.getBytes());
        }
    }

}