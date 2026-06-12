package com.example.hospitalManagement.api;

import com.example.hospitalManagement.entity.Appointments;
import com.example.hospitalManagement.entity.User;
import com.example.hospitalManagement.repository.UserRepository;
import com.example.hospitalManagement.service.AppointmentServiceV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
public class AppointmentApiV2 {

    @Autowired
    private AppointmentServiceV2 appointmentServiceV2;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/doctor/appointments")
    public ResponseEntity<?> getDoctorAppointments(Authentication authentication, @RequestParam(required = false) String status) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String doctorUsername = authentication.getName();

        Optional<User> optUser = userRepository.findByUserName(doctorUsername);
        if (optUser.isEmpty() || optUser.get().getDoctor() == null) {
            return ResponseEntity.badRequest().body("Doctor not found");
        }
        Long doctorId = optUser.get().getDoctor().getId();

        List<Appointments> appointmentsList;
        if (status != null && !status.trim().isEmpty()) {
            try {
                appointmentsList = appointmentServiceV2.getAppointmentStatus(status, doctorId);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Invalid status: " + status);
            }
        } else {
            appointmentsList = appointmentServiceV2.getAllAppointments();
        }
        
        // Filter by current doctor and map to a DTO to avoid JSON infinite recursion
        List<Map<String, Object>> response = appointmentsList.stream()
                .filter(a -> a.getDoctor() != null && a.getDoctor().getUser() != null && a.getDoctor().getUser().getUserName().equals(doctorUsername))
                .map(a -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", a.getId());
                    if (a.getPatient() != null && a.getPatient().getUser() != null) {
                        map.put("patientName", a.getPatient().getUser().getFullName());
                    } else {
                        map.put("patientName", "Unknown");
                    }
                    map.put("date", a.getAppointmentDate() != null ? a.getAppointmentDate().format(DateTimeFormatter.ISO_DATE) : "");
                    map.put("time", a.getAppointmentTime() != null ? a.getAppointmentTime().toString() : "");
                    map.put("reason", a.getReason());
                    map.put("status", a.getStatus().name());
                    return map;
                })
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(response);
    }

    @PutMapping("/doctor/appointments/{id}/accept")
    public ResponseEntity<?> acceptAppointment(@PathVariable Long id) {
        try {
            appointmentServiceV2.acceptAppointment(id);
            return ResponseEntity.ok(Map.of("message", "Appointment accepted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/booking-info-current")
    public ResponseEntity<?> bookAppointment(Authentication authentication){
        String username = authentication.getName();
        Optional<User> user= userRepository.findByUserName(username);
        if(user.isPresent()){
            Map<String,String>map = new HashMap<>();
            map.put("fullName", user.get().getFullName());
            map.put("phone", user.get().getPhone());
            map.put("email", user.get().getEmail());
            return ResponseEntity.ok(map);
        }
        return ResponseEntity.badRequest().build();
    }
}
