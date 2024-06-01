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

        return "redirect:/version-info";

    }

    @GetMapping("/download-update")
    public ResponseEntity<Resource> download() throws IOException {
        Path filePath = Paths.get("/app/update.zip");
        if (Files.exists(filePath)) {
            Resource file = new InputStreamResource(Files.newInputStream(filePath));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=update.zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/x-zip-compressed"))
                    .body(file);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    @PostMapping("/upload-update")
    public ResponseEntity<Void> uploadUpdate(
            @RequestParam("jsonFile") MultipartFile jsonFile,
            @RequestParam("exeFile") MultipartFile exeFile) {

        try {
            // Обробка завантаженого JSON файлу
            Path jsonFilePath = Paths.get("/app/" + jsonFile.getOriginalFilename());
            Files.createDirectories(jsonFilePath.getParent());
            Files.write(jsonFilePath, jsonFile.getBytes());

            // Обробка завантаженого .exe файлу
            if (exeFile != null && !exeFile.isEmpty() &&
                    (Objects.equals(exeFile.getContentType(), "application/octet-stream") ||
                            Objects.equals(exeFile.getContentType(), "application/x-msdownload"))) {
                Path exeFilePath = Paths.get("/app/" + exeFile.getOriginalFilename());
                Files.createDirectories(exeFilePath.getParent());
                Files.write(exeFilePath, exeFile.getBytes());
            }
        } catch(IOException ex) {
            ex.fillInStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/download"));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

}