package com.example.hospitalManagement.controller;

import com.example.hospitalManagement.dto.DepartmentDTO;
import com.example.hospitalManagement.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/")
    public String Index(Authentication authentication, Model model) {

        if (authentication != null && authentication.isAuthenticated()) {
            boolean isDoctor = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("DOCTOR"));
            boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
            if (isAdmin) {
                model.addAttribute("departments", departmentService.getListDepartments());
                model.addAttribute("DepartmentDTO",
                        new DepartmentDTO());
                return "/Dashboard/AdminDashboard";
            }
            if (isDoctor) {
                return "/Dashboard/DoctorDashboard";
            }
            return "/pages/index";

        }
        return "redirect:/login";

    }

}
