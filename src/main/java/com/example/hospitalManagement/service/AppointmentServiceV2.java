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
    private com.example.hospitalManagement.repository.UserRepository userRepository;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ObjectMapper objectMapper;

    public Appointments createAppointment(AppointmentRequest request){
        Long patientId = null;
        
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String username = auth.getName();
            com.example.hospitalManagement.entity.User loggedInUser = userRepository.findByUserName(username).orElse(null);
            
            if (loggedInUser != null) {
                if (loggedInUser.getPatient() != null) {
                    patientId = loggedInUser.getPatient().getId();
                } else {
                    Patient newPatient = new Patient();
                    newPatient.setUser(loggedInUser);
                    newPatient.setCreatedAt(java.time.LocalDate.now());
                    newPatient.setPatientCode("PAT-" + System.currentTimeMillis());
                    patientRepository.save(newPatient);
                    patientId = newPatient.getId();
                }
            }
        }
        
        if (patientId == null && request.getFullname() != null && !request.getFullname().trim().isEmpty()) {
            patientId = patientRepository.findIdByUserFullName(request.getFullname().trim());
        }
        
        if (patientId == null) {
            com.example.hospitalManagement.entity.User guestUser = new com.example.hospitalManagement.entity.User();
            guestUser.setFullName(request.getFullname());
            guestUser.setPhone(request.getSoDienThoai());
            guestUser.setEmail(request.getEmail());
            guestUser.setUserName("guest_" + System.currentTimeMillis());
            guestUser.setPassword("guest");
            guestUser.setStatus(com.example.hospitalManagement.entity.Enum.UserStatus.ACTIVE);
            guestUser.setCreatedAt(LocalDateTime.now());
            userRepository.save(guestUser);

            Patient guestPatient = new Patient();
            guestPatient.setUser(guestUser);
            guestPatient.setCreatedAt(java.time.LocalDate.now());
            guestPatient.setPatientCode("GUEST-" + System.currentTimeMillis());
            patientRepository.save(guestPatient);

            patientId = guestPatient.getId();
        }

        Long doctorId = null;
        if (request.getBacSi() != null && !request.getBacSi().trim().isEmpty()) {
            doctorId = doctorRepository.findIdByUserFullName(request.getBacSi().trim());
        }

        Appointments appointments = new Appointments();
        appointments.setAppointmentDate(request.getDate());
        appointments.setAppointmentTime(request.getTime());

        if (doctorId != null) {
            Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
            appointments.setDoctor(doctor);
        }
        
        Patient finalPatient = patientRepository.findById(patientId).orElse(null);
        appointments.setPatient(finalPatient);

        // Add the guest info into reason so it's not lost if patient is null
        String fullReason = request.getReason();
        if (patientId == null) {
            fullReason = "Khách: " + request.getFullname() + " - SĐT: " + request.getSoDienThoai() + " - " + fullReason;
        }

        appointments.setReason(fullReason);
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
