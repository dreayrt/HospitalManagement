package com.example.hospitalManagement.api;

import com.example.hospitalManagement.dto.ChangePasswordDTO;
import com.example.hospitalManagement.entity.User;
import com.example.hospitalManagement.repository.UserRepository;
import com.example.hospitalManagement.service.R2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
public class ProfileApi{
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private R2Service r2Service;
    @GetMapping
    public Map<String,String> getProfile(Authentication authentication)
    {
        String userName=authentication.getName();
        Optional<User> user=userRepository.findByUserName(userName);
        Map<String,String> result= new HashMap<>();
        if(user.isPresent())
        {
            result.put("fullName",user.get().getFullName());
            result.put("email",user.get().getEmail());
            result.put("phone",user.get().getPhone());
            result.put("role",user.get().getRole().getName());
            result.put("userName",user.get().getUserName());
            result.put("password",user.get().getPassword());
            result.put("avatar",user.get().getAvatar());
        }
        System.out.println("result: "+result);
        return result;
    }
    @Autowired
    private PasswordEncoder passwordEncoder;
    @PatchMapping("/update")
    public ResponseEntity<?> updateProfile(Authentication authentication, @RequestBody com.example.hospitalManagement.dto.ProfileUpdateDTO request) {
        String userName = authentication.getName();
        Optional<User> user = userRepository.findByUserName(userName);
        if (user.isPresent()) {
            User u = user.get();
            if (request.getFullName() != null && !request.getFullName().isEmpty()) {
                u.setFullName(request.getFullName());
            }
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                // Kiểm tra xem email/username mới đã tồn tại chưa để tránh lỗi
                Optional<User> existing = userRepository.findByUserName(request.getEmail());
                if (existing.isPresent() && existing.get().getId() != u.getId()) {
                    return ResponseEntity.badRequest().body("Email đã được sử dụng");
                }
                u.setEmail(request.getEmail());
                u.setUserName(request.getEmail());
            }
            if (request.getPhone() != null && !request.getPhone().isEmpty()) {
                u.setPhone(request.getPhone());
            }
            userRepository.save(u);
            return ResponseEntity.ok("Profile updated");
        }
        return ResponseEntity.badRequest().body("User not found");
    }

    @PatchMapping("/changePassword")
    public ResponseEntity<?>  changePassword (Authentication authentication, @RequestBody ChangePasswordDTO request)
    {
        String userName=authentication.getName();
        Optional<User> user=userRepository.findByUserName(userName);
        if (user.isPresent())
        {
            user.get().setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user.get());
            return ResponseEntity.ok(
                    "Password updated"
            );
        }
        return ResponseEntity.badRequest()
                .body("User not found");
    }

    @PostMapping(value = "/avatar", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> uploadAvatar(@RequestParam("file") MultipartFile file, Authentication authentication) throws IOException
    {
        String image= r2Service.uploadImage(file);
        Optional<User> user=userRepository.findByUserName(authentication.getName());
        if (user.isPresent())
        {
            user.get().setAvatar(image);
            userRepository.save(user.get());
            return ResponseEntity.ok(image);
        }
        return ResponseEntity.badRequest()
                .body("User not found");
    }

}
