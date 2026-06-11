package com.example.hospitalManagement.controller;

import com.example.hospitalManagement.dto.AppointmentRequest;
import com.example.hospitalManagement.dto.CreateMedicalRecordRequest;
import com.example.hospitalManagement.service.MedicalReCordServiceV2;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MedicalRecordController {
    @Autowired
    private MedicalReCordServiceV2 medicalReCordServiceV2;
    @PostMapping("/doctor/medicalRecord")
    public ResponseEntity<?> createMedicalRecord(@Valid @ModelAttribute CreateMedicalRecordRequest createMedicalRecordRequest, BindingResult bindingResult) {
      if(bindingResult.hasErrors()) {
          List<String> errors = bindingResult.getFieldErrors().stream().map(error -> error.getDefaultMessage()).toList();
          return ResponseEntity.badRequest().body(errors);
      }
        medicalReCordServiceV2.createMedicalReCord(createMedicalRecordRequest);
        return ResponseEntity.ok("Tạo hồ sơ thành công");
    }
}
