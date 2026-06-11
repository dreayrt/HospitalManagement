package com.example.hospitalManagement.mapper;

import com.example.hospitalManagement.dto.MedicalRecordDTO;
import com.example.hospitalManagement.entity.MedicalRecords;
import org.springframework.stereotype.Component;




@Component
public class MedicalRecordMapper {

    public MedicalRecordDTO toDTO(MedicalRecords record) {
        if (record == null) return null;
        MedicalRecordDTO dto = new MedicalRecordDTO();
        dto.setId(record.getId());
        if (record.getAppointment() != null) {
            dto.setAppointmentId(record.getAppointment().getId());
        }
        if (record.getDoctor() != null) {
            dto.setDoctorId(record.getDoctor().getId());
        }
        if (record.getPatient() != null) {
            dto.setPatientId(record.getPatient().getId());
        }
        dto.setDiagnosis(record.getDiagnosis());
        dto.setTreatmentPlan(record.getTreatmentPlan());
        dto.setNotes(record.getNotes());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }
}
