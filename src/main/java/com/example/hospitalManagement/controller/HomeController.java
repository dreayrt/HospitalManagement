package com.example.hospitalManagement.controller;

import com.example.hospitalManagement.dto.CreateMedicalRecordRequest;
import com.example.hospitalManagement.dto.DepartmentDTO;
import com.example.hospitalManagement.service.DepartmentService;
import com.example.hospitalManagement.repository.UserRepository;
import com.example.hospitalManagement.repository.AppointmentRepository;
import com.example.hospitalManagement.repository.PatientRepository;
import com.example.hospitalManagement.entity.User;
import com.example.hospitalManagement.entity.Doctor;
import com.example.hospitalManagement.entity.Enum.AppointmentStatus;
import com.example.hospitalManagement.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Optional;

@Controller
public class HomeController {
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private com.example.hospitalManagement.repository.MedicalRecordRepository medicalRecordRepository;
    @Autowired
    private RoomService roomService;
    @Autowired
    private PatientRepository patientRepository;

    @GetMapping("/")
    public String Index(Authentication authentication, Model model, @ModelAttribute CreateMedicalRecordRequest createMedicalRecordRequest) {

        if (authentication != null && authentication.isAuthenticated()) {
            boolean isDoctor = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("DOCTOR"));
            boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));
            if (isAdmin) {
                model.addAttribute("departments", departmentService.getListDepartments());
                model.addAttribute("DepartmentDTO",
                        new DepartmentDTO());
                
                // Load recent patients
                java.util.List<com.example.hospitalManagement.entity.Patient> recentPatientsEntities = patientRepository.findTop5ByOrderByIdDesc();
                java.util.List<java.util.Map<String, Object>> recentPatients = new java.util.ArrayList<>();
                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                
                for(com.example.hospitalManagement.entity.Patient p : recentPatientsEntities) {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    
                    String fullName = p.getUser() != null && p.getUser().getFullName() != null ? p.getUser().getFullName() : "Chưa cập nhật";
                    map.put("fullName", fullName);
                    
                    String initials = "BN";
                    if(fullName != null && !fullName.equals("Chưa cập nhật")){
                        String[] parts = fullName.split(" ");
                        if(parts.length > 0) {
                            initials = parts[parts.length-1].substring(0, Math.min(2, parts[parts.length-1].length())).toUpperCase();
                        }
                    }
                    map.put("initials", initials);
                    
                    String ageGender = "Không rõ";
                    if (p.getDateOfBirth() != null) {
                        int age = java.time.LocalDate.now().getYear() - p.getDateOfBirth().getYear();
                        ageGender = age + " tuổi";
                    }
                    map.put("ageGender", ageGender);
                    map.put("patientCode", p.getPatientCode() != null ? p.getPatientCode() : "Chưa có mã");
                    
                    String department = "Chưa xếp khoa";
                    String doctorName = "Chưa xếp bác sĩ";
                    String status = "Đăng ký mới";
                    String statusClass = "status-info";
                    
                    if(p.getMedicalRecords() != null && !p.getMedicalRecords().isEmpty()) {
                        com.example.hospitalManagement.entity.MedicalRecords latestRecord = p.getMedicalRecords().get(p.getMedicalRecords().size() - 1);
                        if(latestRecord.getDoctor() != null) {
                            if(latestRecord.getDoctor().getUser() != null) {
                                doctorName = "BS. " + latestRecord.getDoctor().getUser().getFullName();
                            }
                            if(latestRecord.getDoctor().getDepartment() != null) {
                                department = latestRecord.getDoctor().getDepartment().getName();
                            }
                        }
                        status = "Đang khám";
                        statusClass = "status-warning";
                        
                        if (p.getRoomPatients() != null && !p.getRoomPatients().isEmpty()) {
                            status = "Nội trú";
                            statusClass = "status-danger";
                        }
                    }
                    
                    map.put("department", department);
                    map.put("doctorName", doctorName);
                    map.put("status", status);
                    map.put("statusClass", statusClass);
                    map.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().format(dtf) : "N/A");
                    
                    // index for background color styling
                    map.put("bgClass", "bg-" + ((p.getId() % 5) + 1));
                    
                    recentPatients.add(map);
                }
                model.addAttribute("recentPatients", recentPatients);

                return "/Dashboard/AdminDashboard";
            }
            if (isDoctor) {
                String username = authentication.getName();
                Optional<User> optUser = userRepository.findByUserName(username);
                if (optUser.isPresent() && optUser.get().getDoctor() != null) {
                    Doctor doctor = optUser.get().getDoctor();
                    model.addAttribute("doctorName", optUser.get().getFullName());
                    model.addAttribute("doctorDepartment", doctor.getDepartment() != null ? doctor.getDepartment().getName() : "Khoa chung");
                    
                    String initials = "";
                    if(optUser.get().getFullName() != null && !optUser.get().getFullName().isEmpty()){
                        String[] parts = optUser.get().getFullName().split(" ");
                        initials = (parts.length > 0 ? parts[parts.length-1].substring(0, Math.min(2, parts[parts.length-1].length())) : "BS").toUpperCase();
                    } else {
                        initials = "BS";
                    }
                    model.addAttribute("doctorInitials", initials);

                    java.util.List<com.example.hospitalManagement.entity.MedicalRecords> medicalRecords = medicalRecordRepository.findByDoctorId(doctor.getId());
                    model.addAttribute("medicalRecords", medicalRecords);

                    long totalPatients = medicalRecords.size();
                    model.addAttribute("totalPatients", totalPatients);

                    long todayAppointments = appointmentRepository.findByDoctorIdAndAppointmentDate(doctor.getId(), LocalDate.now()).size();
                    model.addAttribute("todayAppointments", todayAppointments);

                    long totalDoctorAppointments = appointmentRepository.findAll().stream()
                            .filter(a -> a.getDoctor() != null && a.getDoctor().getId() == doctor.getId() && a.getStatus() == AppointmentStatus.PENDING)
                            .count();
                    model.addAttribute("newAppointments", totalDoctorAppointments);
                    
                    long pendingPrescriptions = appointmentRepository.findAll().stream()
                            .filter(a -> a.getDoctor() != null && a.getDoctor().getId() == doctor.getId() && a.getStatus() == AppointmentStatus.CONFIRMED)
                            .count();
                    model.addAttribute("pendingPrescriptions", pendingPrescriptions);
                    
                    model.addAttribute("doctorAvatar", optUser.get().getAvatar());

                    model.addAttribute("rooms", roomService.getAllRooms());

                    model.addAttribute("activeTreatments", medicalRecords.size()); 
                }
                model.addAttribute("CreateMedicalRecordRequest", createMedicalRecordRequest);
                return "/Dashboard/DoctorDashboard";
            }
            model.addAttribute("appointmentRequest", new com.example.hospitalManagement.dto.AppointmentRequest());
            return "/pages/index";

        }
        return "redirect:/login";

    }
    @GetMapping("/index")
    public String index(Model model){
        model.addAttribute("appointmentRequest", new com.example.hospitalManagement.dto.AppointmentRequest());
        return "pages/index";
    }





}
