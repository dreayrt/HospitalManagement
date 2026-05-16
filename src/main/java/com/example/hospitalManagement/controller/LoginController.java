package com.example.hospitalManagement.controller;

import com.example.hospitalManagement.dto.LoginDTO;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String loginPage( Model model) {
        model.addAttribute("LoginDTO", new LoginDTO());
        return "pages/LoginPage";
    }
}
