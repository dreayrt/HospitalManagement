package com.example.hospitalManagement.service;

import com.example.hospitalManagement.dto.MedicalRecordDTO;
import com.example.hospitalManagement.entity.MedicalRecords;
import com.example.hospitalManagement.entity.Appointments;
import com.example.hospitalManagement.entity.Doctor;
import com.example.hospitalManagement.entity.Patient;
import com.example.hospitalManagement.entity.Enum.AppointmentStatus;
import com.example.hospitalManagement.repository.MedicalRecordRepository;
import com.example.hospitalManagement.repository.AppointmentRepository;
import com.example.hospitalManagement.repository.DoctorRepository;
import com.example.hospitalManagement.repository.PatientRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicalRecordService {

    
    private static final String KEY_MEDICAL_HISTORY = "medical:history:patient:";
    private static final long CACHE_TTL_SECONDS = 120; 

    private final MedicalRecordRepository medicalRecordRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private RedisService redisService; 

    private final ObjectMapper objectMapper;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }


    @Transactional
    public MedicalRecordDTO createRecord(MedicalRecordDTO dto) {
        MedicalRecords record = new MedicalRecords();
        
        if (dto.getAppointmentId() != null) {
            Appointments appt = appointmentRepository.findById(dto.getAppointmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cuộc hẹn ID: " + dto.getAppointmentId()));
            record.setAppointment(appt);
        }
        if (dto.getDoctorId() != null) {
            Doctor doc = doctorRepository.findById(dto.getDoctorId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ ID: " + dto.getDoctorId()));
            record.setDoctor(doc);
        }
        if (dto.getPatientId() != null) {
            Patient pat = patientRepository.findById(dto.getPatientId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bệnh nhân ID: " + dto.getPatientId()));
            record.setPatient(pat);
        }

        record.setDiagnosis(dto.getDiagnosis());
        record.setSymptoms(dto.getSymptoms());
        record.setTreatmentPlan(dto.getTreatmentPlan());
        record.setNotes(dto.getNotes());
        record.setCreatedAt(LocalDateTime.now());

        MedicalRecords saved = medicalRecordRepository.save(record);

        
        String cacheKey = KEY_MEDICAL_HISTORY + dto.getPatientId() + ":page*";
        
        redisService.remove(KEY_MEDICAL_HISTORY + dto.getPatientId() + ":p" + 0);

        return mapToDTO(saved);
    }

    


    @Transactional(readOnly = true)
    public Page<MedicalRecordDTO> getHistoryByPatient(Long patientId, Pageable pageable) {
        
        String cacheKey = KEY_MEDICAL_HISTORY + patientId + ":p" + pageable.getPageNumber();

        
        try {
            if (redisService.exists(cacheKey)) {
                Object rawData = redisService.get(cacheKey);
                if (rawData != null) {
                    List<MedicalRecordDTO> cachedList = objectMapper.readValue(
                            rawData.toString(),
                            new TypeReference<List<MedicalRecordDTO>>() {}
                    );
                    return new PageImpl<>(cachedList, pageable, cachedList.size());
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi đọc Redis Cache bệnh án: " + e.getMessage());
            
        }

        
        Page<MedicalRecords> page = medicalRecordRepository.findByPatientId(patientId, pageable);
        Page<MedicalRecordDTO> result = page.map(this::mapToDTO);

        
        try {
            String json = objectMapper.writeValueAsString(result.getContent());
            redisService.save(cacheKey, json, CACHE_TTL_SECONDS);
        } catch (Exception ignored) {}

        return result;
    }

    @Transactional(readOnly = true)
    public long countMedicalRecords(String search, AppointmentStatus status) {
        return medicalRecordRepository.countWithSearch(search, status);
    }

    @Transactional(readOnly = true)
    public Page<MedicalRecordDTO> getMedicalRecords(String search, AppointmentStatus status, Pageable pageable) {
        return medicalRecordRepository.findWithSearch(search, status, pageable).map(this::mapToDTO);
    }

    private MedicalRecordDTO mapToDTO(MedicalRecords record) {
        MedicalRecordDTO dto = new MedicalRecordDTO();
        dto.setId(record.getId());
        if (record.getAppointment() != null) {
            dto.setAppointmentId(record.getAppointment().getId());
            dto.setAppointmentDate(record.getAppointment().getAppointmentDate());
            dto.setAppointmentTime(record.getAppointment().getAppointmentTime());
            dto.setAppointmentStatus(record.getAppointment().getStatus());
        }
        if (record.getDoctor() != null) {
            dto.setDoctorId(record.getDoctor().getId());
            if (record.getDoctor().getUser() != null) {
                dto.setDoctorName(record.getDoctor().getUser().getFullName());
            }
            if (record.getDoctor().getDepartment() != null) {
                dto.setDepartmentName(record.getDoctor().getDepartment().getName());
            }
        }
        if (record.getPatient() != null) {
            dto.setPatientId(record.getPatient().getId());
            dto.setPatientCode(record.getPatient().getPatientCode());
            if (record.getPatient().getUser() != null) {
                dto.setPatientName(record.getPatient().getUser().getFullName());
                dto.setPatientPhone(record.getPatient().getUser().getPhone());
            }
        }
        dto.setDiagnosis(record.getDiagnosis());
        dto.setSymptoms(record.getSymptoms());
        dto.setTreatmentPlan(record.getTreatmentPlan());
        dto.setNotes(record.getNotes());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }
}
