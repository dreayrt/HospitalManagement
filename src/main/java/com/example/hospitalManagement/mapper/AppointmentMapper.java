package com.example.hospitalManagement.mapper;

import com.example.hospitalManagement.dto.AppointmentDTO;
import com.example.hospitalManagement.entity.Appointments;
import org.springframework.stereotype.Component;

/**
 * Mapper chuyển đổi giữa entity Appointments và AppointmentDTO
 */
@Component
public class AppointmentMapper {

    /**
     * Chuyển entity Appointments → AppointmentDTO để trả về cho frontend
     */
    public AppointmentDTO toDTO(Appointments a) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(a.getId());
        dto.setAppointmentDate(a.getAppointmentDate());
        dto.setAppointmentTime(a.getAppointmentTime());
        dto.setReason(a.getReason());
        dto.setStatus(a.getStatus());
        dto.setCreatedAt(a.getCreatedAt());

        // Thông tin bệnh nhân
        if (a.getPatient() != null) {
            dto.setPatientId(a.getPatient().getId());
            dto.setPatientCode(a.getPatient().getPatientCode());
            // Tên bệnh nhân lấy từ User liên kết
            if (a.getPatient().getUser() != null) {
                dto.setPatientName(a.getPatient().getUser().getFullName());
                dto.setPatientPhone(a.getPatient().getUser().getPhone());
            }
        }

        // Thông tin bác sĩ
        if (a.getDoctor() != null) {
            dto.setDoctorId(a.getDoctor().getId());
            dto.setDoctorLicenseNumber(a.getDoctor().getLicenseNumber());
            if (a.getDoctor().getUser() != null) {
                dto.setDoctorName(a.getDoctor().getUser().getFullName());
            }
            if (a.getDoctor().getDepartment() != null) {
                dto.setDepartmentName(a.getDoctor().getDepartment().getName());
            }
        }

        // Người tạo lịch
        if (a.getCreatedBy() != null) {
            dto.setCreatedByName(a.getCreatedBy().getFullName());
        }

        return dto;
    }
}
