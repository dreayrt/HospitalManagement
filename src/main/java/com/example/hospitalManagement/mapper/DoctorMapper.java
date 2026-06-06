package com.example.hospitalManagement.mapper;

import com.example.hospitalManagement.dto.DoctorDTO;
import com.example.hospitalManagement.dto.DoctorScheduleDTO;
import com.example.hospitalManagement.entity.Doctor;
import com.example.hospitalManagement.entity.DoctorSchedules;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {

    
    public DoctorDTO toDoctorDTO(Doctor doctor, String workingStatus, long todayScheduleCount, long totalScheduleCount) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doctor.getId());
        if (doctor.getUser() != null) {
            dto.setUserId(doctor.getUser().getId());
            dto.setFullName(doctor.getUser().getFullName());
            dto.setEmail(doctor.getUser().getEmail());
            dto.setPhone(doctor.getUser().getPhone());
        }
        dto.setLicenseNumber(doctor.getLicenseNumber());
        dto.setExperienceYears(doctor.getExperienceYears());
        dto.setStatus(doctor.getUserStatus()); 
        dto.setSpecialization(doctor.getSpecialization());

        if (doctor.getDepartment() != null) {
            dto.setDepartmentId(doctor.getDepartment().getId());
        }
        dto.setWorkingStatus(workingStatus);
        dto.setTodayScheduleCount(todayScheduleCount);
        dto.setTotalScheduleCount(totalScheduleCount);
        return dto;
    }

    public DoctorScheduleDTO toScheduleDTO(DoctorSchedules schedule) {
        DoctorScheduleDTO dto = new DoctorScheduleDTO();
        dto.setId(schedule.getId());
        dto.setWorkDate(schedule.getWorkDate());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setStatus(schedule.getStatus());
        if (schedule.getDoctor() != null) {
            dto.setDoctorId(schedule.getDoctor().getId());
            if (schedule.getDoctor().getUser() != null) {
                dto.setDoctorName(schedule.getDoctor().getUser().getFullName());
            }
        }
        return dto;
    }
}
