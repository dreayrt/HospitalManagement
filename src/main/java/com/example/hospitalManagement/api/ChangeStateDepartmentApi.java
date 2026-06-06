package com.example.hospitalManagement.api;

import com.example.hospitalManagement.repository.DepartmentRepository;
import com.example.hospitalManagement.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChangeStateDepartmentApi {
    @Autowired
    private DepartmentService departmentService;
    @PutMapping("/departments/change-status/{id}")
    @ResponseBody
    public ResponseEntity<?> changeStatus(
            @PathVariable Long id
    ){

        departmentService.changeStatus(id);

        return ResponseEntity.ok().build();
    }
}
