package com.example.hospitalManagement.controller;

import com.example.hospitalManagement.dto.DepartmentDTO;
import com.example.hospitalManagement.entity.Departments;
import com.example.hospitalManagement.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;

    @PostMapping("/addDepartment")
    @ResponseBody
    public ResponseEntity<?> addDepartment(
            @Valid @RequestBody DepartmentDTO dto,
            BindingResult bindingResult
    ) {

        if (bindingResult.hasErrors()) {

            Map<String, String> errors =
                    new HashMap<>();

            bindingResult.getFieldErrors()
                    .forEach(error -> {

                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        );
                    });

            return ResponseEntity
                    .badRequest()
                    .body(errors);
        }

        departmentService.addDepartment(dto);

        return ResponseEntity.ok().build();
    }
}
