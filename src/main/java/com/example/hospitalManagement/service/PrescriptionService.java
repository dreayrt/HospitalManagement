package com.example.hospitalManagement.service;

import com.example.hospitalManagement.entity.Appointments;
import com.example.hospitalManagement.entity.MedicalRecords;
import com.example.hospitalManagement.entity.Prescription;
import com.example.hospitalManagement.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrescriptionService {
    @Autowired
    private PrescriptionRepository prescriptionRepository;
    public Prescription createPrescription(Appointments appointments, MedicalRecords medicalRecords) {
        Prescription prescription = new Prescription();
        prescription.setDoctor(appointments.getDoctor());
        prescription.setPatient(appointments.getPatient());
        prescription.setMedicalRecords(medicalRecords);
        prescription.setCreatedAt(appointments.getCreatedAt());
        prescriptionRepository.save(prescription);
        return prescription;
    }
}
