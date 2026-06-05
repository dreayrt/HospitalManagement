package com.example.hospitalManagement.api;

import com.example.hospitalManagement.dto.MedicalRecordDTO;
import com.example.hospitalManagement.entity.Enum.AppointmentStatus;
import com.example.hospitalManagement.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;





@RestController
@RequestMapping("/api/medical-records")
@CrossOrigin(origins = "*")
public class MedicalRecordApi {

    @Autowired
    private MedicalRecordService medicalRecordService;
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        AppointmentStatus parsedStatus = parseStatus(status);
        long totalRecords = medicalRecordService.countMedicalRecords(search, parsedStatus);
        return ResponseEntity.ok(Map.of("totalRecords", totalRecords));
    }

    @GetMapping
    public ResponseEntity<Page<MedicalRecordDTO>> getMedicalRecords(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(medicalRecordService.getMedicalRecords(search, parseStatus(status), pageable));
    }

    private AppointmentStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return AppointmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @PostMapping
    public ResponseEntity<?> createRecord(@RequestBody MedicalRecordDTO dto) {
        try {
            if (dto.getPatientId() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "ID bệnh nhân là bắt buộc."));
            }
            if (dto.getDiagnosis() == null || dto.getDiagnosis().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Chẩn đoán bệnh án là bắt buộc."));
            }
            MedicalRecordDTO result = medicalRecordService.createRecord(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Lỗi máy chủ: " + e.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<Page<MedicalRecordDTO>> getPatientHistory(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<MedicalRecordDTO> history = medicalRecordService.getHistoryByPatient(patientId, pageable);
        return ResponseEntity.ok(history);
    }
}
