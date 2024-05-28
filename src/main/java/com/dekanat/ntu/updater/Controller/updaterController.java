package com.dekanat.ntu.updater.Controller;

import com.dekanat.ntu.updater.Entity.VersionInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@RestController
public class updaterController {

    @GetMapping("/check-update")
    public String checkUpdate() throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        Resource resource = new ClassPathResource("/update/version.json");
        VersionInfo versionInfo = mapper.readValue(resource.getFile(), VersionInfo.class);

        return versionInfo.getVersion();
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download() throws IOException {
        ClassPathResource file = new ClassPathResource("/update/update.zip");
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=update.zip");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType
                        .parseMediaType("application/x-zip-compressed"))
                .body(new InputStreamResource(file
                        .getInputStream()));
    }

    @PostMapping("/upload-update")
    public String uploadUpdate(@RequestParam("jsonFile") MultipartFile jsonFile, @RequestParam("zipFile") MultipartFile zipFile)  {
        try {
            // Обробка завантаженого JSON файлу
            Path jsonFilePath = Paths.get("/app/update/" + jsonFile.getOriginalFilename());
            Files.createDirectories(jsonFilePath.getParent());
            Files.write(jsonFilePath, jsonFile.getBytes());

            if (zipFile != null && !zipFile.isEmpty() && Objects.equals(zipFile.getContentType(), "application/x-zip-compressed")) {
                Path zipFilePath = Paths.get("/app/update", zipFile.getOriginalFilename());
                Files.write(zipFilePath, zipFile.getBytes());
            }
        } catch(IOException ex) {
            ex.fillInStackTrace();
            return "error";
        }

        return "redirect:upload";
    }

}
