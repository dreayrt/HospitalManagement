package com.example.hospitalManagement.service;

import com.example.hospitalManagement.dto.registerDTO;
import com.example.hospitalManagement.entity.Role;
import com.example.hospitalManagement.entity.User;
import com.example.hospitalManagement.entity.Enum.UserStatus;
import com.example.hospitalManagement.repository.RoleRepository;
import com.example.hospitalManagement.repository.UserRepository;
import com.example.hospitalManagement.util.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RegisterService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    
    @Transactional
    public User registerUser(registerDTO dto) {
        User user = new User();
        user.setFullName(dto.getFullName());
        user.setPassword(HashUtil.sha256(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setUserName(dto.getUserName());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(null);
        user.setStatus(UserStatus.ACTIVE);
        
        Role patientRole = roleRepository.findByName("PATIENT")
            .orElseThrow(() -> new RuntimeException("Role PATIENT not found"));
        user.setRole(patientRole);

        return userRepository.save(user);

    }
}
