package com.example.hospitalManagement.api;

import com.example.hospitalManagement.dto.CreateMedicalRecordRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("prescribeMedicationApi")
public class PrescribeMedication {
    @PostMapping()
    public ResponseEntity<?> prescribeMedication(@RequestBody CreateMedicalRecordRequest createMedicalRecordRequest) {
       return ResponseEntity.ok().build();
    }
}
