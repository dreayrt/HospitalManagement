package com.example.hospitalManagement.service;

import com.example.hospitalManagement.dto.CreateMedicalRecordRequest;
import com.example.hospitalManagement.dto.PrescriptionDetailDTO;
import com.example.hospitalManagement.entity.Appointments;
import com.example.hospitalManagement.entity.MedicalRecords;
import com.example.hospitalManagement.entity.Prescription;
import com.example.hospitalManagement.entity.PrescriptionDetail;
import com.example.hospitalManagement.repository.AppointmentRepository;
import com.example.hospitalManagement.repository.MedicalRecordRepository;
import com.example.hospitalManagement.repository.PrescriptionDetailRepository;
import com.example.hospitalManagement.repository.PrescriptionRepository;
import com.example.hospitalManagement.entity.Enum.AppointmentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MedicalReCordServiceV2 {
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    @Autowired
    private PrescriptionService  prescriptionService;
    @Autowired
    private PrescriptionDetailRepository prescriptionDetailRepository;
    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Transactional
    public void createMedicalReCord(CreateMedicalRecordRequest createMedicalRecordRequest) {
        MedicalRecords record = new MedicalRecords();
        record.setDiagnosis(createMedicalRecordRequest.getChuanDoan());
        record.setSymptoms(createMedicalRecordRequest.getTrieuChung());
        record.setTreatmentPlan(createMedicalRecordRequest.getLieuTrinh());
        record.setNotes(createMedicalRecordRequest.getGhiChu());
        record.setCreatedAt(LocalDateTime.now());
        Appointments appointment = appointmentRepository.findById(createMedicalRecordRequest.getAppointmentId()).orElseThrow(()->new RuntimeException("appointment not found"));
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
        record.setAppointment(appointment);
        record.setDoctor(appointment.getDoctor());
        record.setPatient(appointment.getPatient());
        medicalRecordRepository.save(record);

        Prescription prescription= prescriptionService.createPrescription(appointment,record);
        List<PrescriptionDetail> prescriptionDetails = new ArrayList<>();
        for (PrescriptionDetailDTO dto: createMedicalRecordRequest.getPrescriptionDetailDTO()) {
            PrescriptionDetail prescriptionDetail = new PrescriptionDetail();
            prescriptionDetail.setFrequency(dto.getTanSuat());
            prescriptionDetail.setInstruction(dto.getGioiThieu());
            prescriptionDetail.setMedicineName(dto.getMedicineName());
            prescriptionDetail.setDosage(dto.getLieuLuong());
            prescriptionDetail.setDuration(dto.getLieuTrinh());
            prescriptionDetail.setPrescription(prescription);
            prescriptionDetails.add(prescriptionDetail);
        }
        prescriptionDetailRepository.saveAll(prescriptionDetails);
        prescriptionRepository.save(prescription);
    }
}
