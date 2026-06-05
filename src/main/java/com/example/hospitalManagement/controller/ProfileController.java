package com.example.hospitalManagement.controller;

import com.example.hospitalManagement.entity.User;
import com.example.hospitalManagement.repository.userRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class ProfileController {

    @Autowired
    private userRepository userRepository;
    @GetMapping("/profile")
    public String profile () {
        return "pages/Profile";
    }
}
