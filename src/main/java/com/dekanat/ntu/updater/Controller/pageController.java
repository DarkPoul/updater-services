package com.dekanat.ntu.updater.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class pageController {
    @GetMapping("/upload")
    public String getUploadPage() {
        return "upload-files";
    }
}
