package com.example.hospitalManagement.controller;

import com.example.hospitalManagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;
    @GetMapping("/profile")
    public String profile () {
        return "pages/Profile";
    }
}
