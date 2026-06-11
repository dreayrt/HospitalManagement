package com.example.hospitalManagement.service;

import com.example.hospitalManagement.dto.AppointmentRequest;
import com.example.hospitalManagement.entity.Appointments;
import com.example.hospitalManagement.entity.Doctor;
import com.example.hospitalManagement.entity.Enum.AppointmentStatus;
import com.example.hospitalManagement.entity.Patient;
import com.example.hospitalManagement.repository.AppointmentRepository;
import com.example.hospitalManagement.repository.DoctorRepository;
import com.example.hospitalManagement.repository.PatientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentServiceV2 {
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ObjectMapper objectMapper;

    public Appointments createAppointment(AppointmentRequest request){
        long patientId=patientRepository.findIdByUserFullName(request.getFullname());
        long doctorId=doctorRepository.findIdByUserFullName(request.getBacSi());

        Appointments appointments = new Appointments();
        appointments.setAppointmentDate(request.getDate());
        appointments.setAppointmentTime(request.getTime());

        Doctor doctor = doctorRepository.getReferenceById(doctorId);
        Patient patient = patientRepository.getReferenceById(patientId);

        appointments.setDoctor(doctor);
        appointments.setPatient(patient);
        appointments.setReason(request.getReason());
        appointments.setStatus(com.example.hospitalManagement.entity.Enum.AppointmentStatus.PENDING);
        appointments.setCreatedAt(LocalDateTime.now());
        appointmentRepository.save(appointments);

        return appointments;
    }

    public List<Appointments> getAllAppointments(){
        String key="appointments";
        if(redisService.exists(key)){
           try{
                String json= (String) redisService.get(key);
                return objectMapper.readValue(json,objectMapper.getTypeFactory().constructCollectionType(List.class, Appointments.class));
           }catch(Exception e){
                e.printStackTrace();
           }
        }
        List<Appointments> appointments=appointmentRepository.findAll();
        try{
            String json= objectMapper.writeValueAsString(appointments);
            redisService.save(key,json,3600);
        }catch(Exception e){
            e.printStackTrace();
        }
        return appointments;
    }
    public Appointments acceptAppointment(Long id) {
        Appointments appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setStatus(com.example.hospitalManagement.entity.Enum.AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);
        redisService.remove("appointments"); // Clear cache
        return appointment;
    }

    public List<Appointments> getAppointmentStatus(String status, Long doctorId){
        List<Appointments> candidate= appointmentRepository.findByStatusAndDoctorId(AppointmentStatus.valueOf(status.toUpperCase()), doctorId);
        return  candidate;
    }
}
