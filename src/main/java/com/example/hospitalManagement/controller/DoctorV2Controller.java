package com.example.hospitalManagement.controller;

import com.example.hospitalManagement.entity.Doctor;
import com.example.hospitalManagement.service.DoctorService;
import com.example.hospitalManagement.service.DoctorServiceV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DoctorV2Controller {
    @Autowired
    private DoctorServiceV2 doctorServiceV2;
    @Autowired
    private com.example.hospitalManagement.service.DepartmentService departmentService;

    @GetMapping("/bac-si")
    public String doctor(Model model){
        List<Doctor> doctors = doctorServiceV2.getAllDoctors();
        model.addAttribute("doctors", doctors);
        model.addAttribute("departments", departmentService.getListDepartments());
        return "pages/bac_si";
    }

    @GetMapping("/ho-so-bac-si/{id}")
    public String hoSoBacSi(@org.springframework.web.bind.annotation.PathVariable Long id, Model model){
        Doctor doctor = doctorServiceV2.getDoctorById(id);
        if(doctor == null){
            return "redirect:/bac-si";
        }
        model.addAttribute("doctor", doctor);
        model.addAttribute("appointmentRequest", new com.example.hospitalManagement.dto.AppointmentRequest());
        return "pages/ho_so_bac_si";
    }
}
