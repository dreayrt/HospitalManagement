package com.example.hospitalManagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DeniedAccessController {
    @GetMapping("/DeniedAccessPage")
    public String deniedAccessPage(){
        return "/pages/DeniedAccessPage";
    }
}
