package com.example.hospitalManagement.api;

import com.example.hospitalManagement.dto.LoginDTO;
import com.example.hospitalManagement.entity.User;
import com.example.hospitalManagement.repository.userRepository;
import com.example.hospitalManagement.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import javax.naming.Binding;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationApi {
    @Autowired
    private AuthenticationManager  authenticationManager;
    @Autowired
    private userRepository userRepository;
    @Autowired
    private JWTUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> Login(@Valid @ModelAttribute LoginDTO loginDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            System.out.println("Error login post: "+errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUserName(),
                            loginDTO.getPassword()
                    )
            );
        } catch (LockedException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Tài khoản của bạn đã bị khóa!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        catch (BadCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Sai tài khoản hoặc mật khẩu!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        User user = userRepository.findByUserName(loginDTO.getUserName()).orElseThrow(() -> new RuntimeException("Username not found"));
        String role = user.getRole().getName();
        String accessToken = jwtUtil.generateToken(loginDTO.getUserName(), role);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("access_token", accessToken);
        responseData.put("role",role);
        System.out.println("ROLEEEEE: "+role);
        if (Boolean.TRUE.equals(loginDTO.isRememberMe())) {
            String refreshToken = jwtUtil.generateRefreshToken(loginDTO.getUserName());
            responseData.put("refreshToken", refreshToken);
        }
        return ResponseEntity.ok(responseData);

    }
}
