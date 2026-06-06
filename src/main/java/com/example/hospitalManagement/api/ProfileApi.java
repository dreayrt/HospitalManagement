package com.example.hospitalManagement.api;

import com.example.hospitalManagement.dto.ChangePasswordDTO;
import com.example.hospitalManagement.entity.User;
import com.example.hospitalManagement.repository.userRepository;
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
    private userRepository userRepository;
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
