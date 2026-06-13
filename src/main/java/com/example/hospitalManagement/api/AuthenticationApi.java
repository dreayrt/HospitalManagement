package com.example.hospitalManagement.api;

import com.example.hospitalManagement.dto.LoginDTO;
import com.example.hospitalManagement.dto.RefreshTokenDTO;
import com.example.hospitalManagement.entity.User;
import com.example.hospitalManagement.repository.UserRepository;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationApi {
    @Autowired
    private AuthenticationManager  authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private com.example.hospitalManagement.service.EmailService emailService;
    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken( @RequestBody RefreshTokenDTO refreshTokenDTO) {
        String refreshToken = refreshTokenDTO.getRefreshToken();
        if(!jwtUtil.validateToken(refreshToken)) {
            Map<String, String>error=new HashMap<>();
            error.put("error","Refresh token khong hop le hoac da het han");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        String tokenType=jwtUtil.extractType(refreshToken);
        if(!"refresh_token".equals(tokenType)) {
            Map<String, String>error=new HashMap<>();
            error.put("error","Refresh token khong dung dinh dang");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        String username = jwtUtil.extractUsername(refreshToken);
        Optional<User> user=userRepository.findByUserName(username);
        if(user.isPresent()) {
            String role=user.get().getRole().getName();
            String newAccessToken= jwtUtil.generateToken(username,role);
            Map<String,Object>ResponseData=new HashMap<>();
            ResponseData.put("access_token",newAccessToken);
            return ResponseEntity.ok(ResponseData);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }
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
    @PostMapping("/logout")
    public ResponseEntity<?> Logout() {
        Map<String, String> responseData = new HashMap<>();
        responseData.put("message", "Logout successful");
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody com.example.hospitalManagement.dto.ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Email không tồn tại trong hệ thống.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        // Generate 6 digit OTP
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setResetOtp(otp);
        user.setResetOtpExpiry(java.time.LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        // Send email
        emailService.sendOtpEmail(user.getEmail(), otp);

        Map<String, String> responseData = new HashMap<>();
        responseData.put("message", "Mã OTP đã được gửi đến email của bạn.");
        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody com.example.hospitalManagement.dto.VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Email không hợp lệ."));
        }
        if (user.getResetOtp() == null || !user.getResetOtp().equals(request.getOtp())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Mã OTP không chính xác."));
        }

        if (user.getResetOtpExpiry() != null && user.getResetOtpExpiry().isBefore(java.time.LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Mã OTP đã hết hạn."));
        }

        return ResponseEntity.ok(Map.of("message", "Mã OTP hợp lệ."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody com.example.hospitalManagement.dto.ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Email không hợp lệ."));
        }
        // Check OTP again for security
        if (user.getResetOtp() == null || !user.getResetOtp().equals(request.getOtp())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Mã OTP không chính xác."));
        }
        if (user.getResetOtpExpiry() != null && user.getResetOtpExpiry().isBefore(java.time.LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Mã OTP đã hết hạn."));
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        // Clear OTP
        user.setResetOtp(null);
        user.setResetOtpExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Mật khẩu đã được thay đổi thành công."));
    }
}
