package com.example.hospitalManagement.config;

import com.example.hospitalManagement.entity.Enum.UserStatus;
import com.example.hospitalManagement.entity.Role;
import com.example.hospitalManagement.entity.User;
import com.example.hospitalManagement.repository.RoleRepository;
import com.example.hospitalManagement.repository.UserRepository;
import com.example.hospitalManagement.util.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Transactional
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final RoleRepository roleRepository;

    @Autowired
    public OAuth2SuccessHandler(UserRepository userRepository, JWTUtil jwtUtil, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.roleRepository = roleRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email").toString();
        String name = oAuth2User.getAttribute("name").toString();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = userRepository.findByUserName(email).orElse(null);
        }
        if (user == null) {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(name);
            newUser.setUserName(email);
            newUser.setPassword(new BCryptPasswordEncoder().encode(UUID.randomUUID().toString()));
            newUser.setStatus(UserStatus.ACTIVE);
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setUpdatedAt(LocalDateTime.now());
            Role patientRole = roleRepository.findByName("PATIENT").orElseThrow(() -> new RuntimeException("Role PATIENT not found"));
            newUser.setRole(patientRole);
            user = userRepository.save(newUser);
        }
        String jwt = jwtUtil.generateToken(user.getUserName(), user.getRole().getName());
        response.sendRedirect("/oauth2-success?token=" + jwt);
    }

}
