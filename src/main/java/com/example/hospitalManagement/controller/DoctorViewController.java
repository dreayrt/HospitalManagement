package com.example.hospitalManagement.controller;

import com.example.hospitalManagement.service.DoctorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.hospitalManagement.entity.Doctor;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import com.example.hospitalManagement.entity.DoctorSchedule;

@Controller
public class DoctorViewController {

    private final DoctorService doctorService;

    public DoctorViewController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/admin/doctors")
    public String getAllDoctors(Model model) {
        java.util.List<Doctor> doctors = doctorService.getAllDoctors();
        long specCount = doctors.stream()
                .map(Doctor::getSpecialization)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .count();
        model.addAttribute("doctors", doctors);
        model.addAttribute("specCount", specCount);
        model.addAttribute("newDoctor", new Doctor());
        return "pages/doctors";
    }

    @PostMapping("/admin/doctors/add")
    public String addDoctor(@ModelAttribute Doctor doctor) {
        doctorService.createDoctor(doctor);
        return "redirect:/admin/doctors";
    }

    @PostMapping("/admin/doctors/update/{id}")
    public String updateDoctor(@PathVariable Long id, @ModelAttribute Doctor doctor) {
        doctorService.updateDoctor(id, doctor);
        return "redirect:/admin/doctors";
    }

    @PostMapping("/admin/doctors/delete/{id}")
    public String deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return "redirect:/admin/doctors";
    }

    @GetMapping("/doctor/my-schedule")
    public String getMySchedule(Principal principal, Model model) {
        // Parse the username to a user ID. If principal is null, we use a fallback for testing purposes.
        String username = principal != null ? principal.getName() : "1";
        Long userId;
        try {
            userId = Long.parseLong(username);
        } catch (NumberFormatException e) {
            userId = 1L;
        }

        Optional<Doctor> doctorOpt = doctorService.findByUserId(userId);
        if (doctorOpt.isPresent()) {
            List<DoctorSchedule> schedules = doctorService.getSchedulesByDoctor(doctorOpt.get().getId());
            model.addAttribute("personalSchedules", schedules);
        } else {
            model.addAttribute("personalSchedules", Collections.emptyList());
        }

        return "pages/my-schedule";
    }
}
