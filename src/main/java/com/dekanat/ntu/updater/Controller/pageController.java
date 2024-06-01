package com.dekanat.ntu.updater.Controller;

import com.dekanat.ntu.updater.Entity.VersionInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;
import java.io.IOException;

@Controller
public class pageController {
    @GetMapping("/upload")
    public String getUploadPage() {
        return "upload-files";
    }

    @GetMapping("/download")
    public String getVersionInfo(Model model) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("/app/version.json");
        VersionInfo versionInfo = mapper.readValue(file, VersionInfo.class);

        model.addAttribute("version", versionInfo.getVersion());
        return "version-info";
    }
}
