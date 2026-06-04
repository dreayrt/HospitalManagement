    package com.example.hospitalManagement.controller;

    import org.springframework.security.core.Authentication;
    import org.springframework.stereotype.Controller;
    import org.springframework.web.bind.annotation.GetMapping;

    @Controller
    public class HomeController {
    @GetMapping("/DashBoard/AdminDashboard")
    public String adminDashboard(Authentication authentication) {
        return "Dashboard/AdminDashboard";
    }

    @GetMapping({ "/index"})
    public String index() {
        return "pages/index";
    }
    }
