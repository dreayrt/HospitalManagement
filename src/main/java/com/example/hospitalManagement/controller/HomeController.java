    package com.example.hospitalManagement.controller;

    import org.springframework.security.core.Authentication;
    import org.springframework.stereotype.Controller;
    import org.springframework.web.bind.annotation.GetMapping;

    @Controller
    public class HomeController {
        @GetMapping("/DashBoard/AdminDashboard")
        public String index(Authentication authentication) {
            if (authentication!=null && authentication.isAuthenticated()) {
                boolean isAdmin=authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ADMIN"));
                if (isAdmin) {
                    return "Dashboard/AdminDashboard";
                }
            }
            return "pages/index";

        }

    }
