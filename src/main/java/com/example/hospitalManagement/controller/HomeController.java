package com.example.hospitalManagement.controller;

import com.example.hospitalManagement.dto.CreateMedicalRecordRequest;
import com.example.hospitalManagement.dto.DepartmentDTO;
import com.example.hospitalManagement.service.DepartmentService;
import com.example.hospitalManagement.repository.userRepository;
import com.example.hospitalManagement.repository.AppointmentRepository;
import com.example.hospitalManagement.repository.PatientRepository;
import com.example.hospitalManagement.entity.User;
import com.example.hospitalManagement.entity.Doctor;
import com.example.hospitalManagement.entity.Enum.AppointmentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.util.Optional;

@Controller
public class HomeController {
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private userRepository userRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
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

                    long totalPatients = patientRepository.count();
                    model.addAttribute("totalPatients", totalPatients);

                    long todayAppointments = appointmentRepository.findByDoctorIdAndAppointmentDate(doctor.getId(), LocalDate.now()).size();
                    model.addAttribute("todayAppointments", todayAppointments);

                    long totalDoctorAppointments = appointmentRepository.findAll().stream()
                            .filter(a -> a.getDoctor() != null && a.getDoctor().getId() == doctor.getId())
                            .count();
                    model.addAttribute("newAppointments", totalDoctorAppointments);
                    
                    long pendingPrescriptions = appointmentRepository.findAll().stream()
                            .filter(a -> a.getDoctor() != null && a.getDoctor().getId() == doctor.getId() && a.getStatus() == AppointmentStatus.CONFIRMED)
                            .count();
                    model.addAttribute("pendingPrescriptions", pendingPrescriptions);
                    
                    model.addAttribute("doctorAvatar", optUser.get().getAvatar());

                    // Add mock data for active treatments
                    model.addAttribute("activeTreatments", 45); 
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
