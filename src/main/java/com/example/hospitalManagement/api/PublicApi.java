package com.example.hospitalManagement.api;

import com.example.hospitalManagement.entity.Departments;
import com.example.hospitalManagement.entity.Doctor;
import com.example.hospitalManagement.repository.DepartmentRepository;
import com.example.hospitalManagement.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
public class PublicApi {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @GetMapping("/departments")
    public ResponseEntity<List<Map<String, Object>>> getDepartments() {
        List<Departments> departments = departmentRepository.findAll();
        List<Map<String, Object>> response = departments.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            map.put("name", d.getName());
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<Map<String, Object>>> getDoctorsByDepartment(@RequestParam Long departmentId) {
        List<Doctor> doctors = doctorRepository.findByDepartmentId(departmentId);
        List<Map<String, Object>> response = doctors.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            // Format name with "BS." prefix
            String fullName = d.getUser() != null ? d.getUser().getFullName() : "Unknown";
            map.put("name", "BS. " + fullName);
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
}
