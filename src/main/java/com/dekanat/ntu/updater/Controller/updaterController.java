package com.dekanat.ntu.updater.Controller;

import com.dekanat.ntu.updater.Entity.VersionInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ByteArrayResource;
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
    public ResponseEntity<Resource> downloadUpdate() throws IOException {
        Path filePath = Paths.get("/app/dekanat.exe");
        if (Files.exists(filePath)) {
            byte[] fileContent = Files.readAllBytes(filePath);
            Resource file = new ByteArrayResource(fileContent);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dekanat.exe");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(file);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    @GetMapping("/download-installer")
    public ResponseEntity<Resource> downloadInstaller() throws IOException {
        Path filePath = Paths.get("/app/DekanatInstaller.exe");
        if (Files.exists(filePath)) {
            byte[] fileContent = Files.readAllBytes(filePath);
            Resource file = new ByteArrayResource(fileContent);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=DekanatInstaller.exe");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(file);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    @PostMapping("/upload-update")
    public ResponseEntity<Void> uploadUpdate(
            @RequestParam("jsonFile") MultipartFile jsonFile,
            @RequestParam("installerFile") MultipartFile installerFile,
            @RequestParam("updateFile") MultipartFile updateFile) {
        try {
            // Обробка завантаженого JSON файлу
            Path jsonFilePath = Paths.get("/app/" + jsonFile.getOriginalFilename());
            Files.createDirectories(jsonFilePath.getParent());
            Files.write(jsonFilePath, jsonFile.getBytes());

            // Обробка завантаженого інсталятора
            if (installerFile != null && !installerFile.isEmpty()) {
                if (!Objects.equals(installerFile.getContentType(), "application/vnd.microsoft.portable-executable")) {
                    throw new IllegalArgumentException("Невірний тип файлу для інсталятора");
                }
                Path installerFilePath = Paths.get("/app/", installerFile.getOriginalFilename());
                Files.write(installerFilePath, installerFile.getBytes());
            }

            // Обробка завантаженого оновлення (EXE файл)
            if (updateFile != null && !updateFile.isEmpty()) {
                if (!Objects.equals(updateFile.getContentType(), "application/vnd.microsoft.portable-executable")) {
                    throw new IllegalArgumentException("Невірний тип файлу для оновлення");
                }
                Path updateFilePath = Paths.get("/app/", updateFile.getOriginalFilename());
                Files.write(updateFilePath, updateFile.getBytes());
            }
        } catch (IOException | IllegalArgumentException ex) {
            ex.fillInStackTrace(); // Логуємо стек-трейс для отримання інформації про помилку
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/download"));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

}
