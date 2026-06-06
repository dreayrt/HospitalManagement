package com.example.hospitalManagement.controller;

import com.example.hospitalManagement.dto.registerDTO;
import com.example.hospitalManagement.repository.userRepository;
import com.example.hospitalManagement.service.RegisterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegisterController {
    @Autowired
    private userRepository userRepository;
    @Autowired
    private RegisterService registerService;
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDTO", new registerDTO());
        return "pages/RegisterPage";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registerDTO") registerDTO registerDTO, BindingResult bindingResult, Model model) {
        if(userRepository.existsByEmail(registerDTO.getEmail())) {
            bindingResult.rejectValue("email","error.registerDTO","Email đã tồn tại");
        }
        if(userRepository.existsByPhone(registerDTO.getPhone())) {
            bindingResult.rejectValue("phone","error.registerDTO","Số điện thoại đã được đăng ký");
        }
        if(userRepository.existsByUserName(registerDTO.getUserName())) {
            bindingResult.rejectValue("userName","error.registerDTO","Tên đăng nhập đã tồn tại");
        }
        if (bindingResult.hasErrors()) {
            return "pages/RegisterPage";
        }
        registerService.registerUser(registerDTO);
        return "redirect:/login";
    }
}
