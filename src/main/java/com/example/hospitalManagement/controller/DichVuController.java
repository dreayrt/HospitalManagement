package com.example.hospitalManagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DichVuController {

    @GetMapping("/dich-vu")
    public String dichVu(){
        return "Service";
    }

}