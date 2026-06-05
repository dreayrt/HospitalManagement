package com.example.hospitalManagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "pages/index";
    }

    @GetMapping("/index")
    public String index() {
        return "pages/index";
    }

}