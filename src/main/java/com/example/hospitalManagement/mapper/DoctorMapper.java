package com.example.hospitalManagement.mapper;
import com.example.hospitalManagement.dto.DoctorDTO;
import com.example.hospitalManagement.entity.Doctor;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {
    public DoctorDTO toDTO(Doctor doctor) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doctor.getId());
        dto.setLicenseNumber(doctor.getLicenseNumber());
        dto.setSpecialization(doctor.getSpecialization());
        return dto;
    }
}