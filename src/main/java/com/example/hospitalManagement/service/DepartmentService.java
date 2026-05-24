package com.example.hospitalManagement.service;

import com.example.hospitalManagement.dto.DepartmentDTO;
import com.example.hospitalManagement.entity.Departments;
import com.example.hospitalManagement.entity.Enum.DepartmentStatus;
import com.example.hospitalManagement.repository.DepartmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DepartmentService {
    @Autowired
    private RedisService redisService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DepartmentRepository departmentRepository;

    public List<Departments> getListDepartments() {
        String key = "departments";
        if (redisService.exists(key)) {
            try {
                String json = (String) redisService.get(key);
                return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, Departments.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        List<Departments> departmentsList = departmentRepository.findAll();
        try{
            String json = objectMapper.writeValueAsString(departmentsList);
            redisService.save(key,json,300);

        }catch(Exception e){
            e.printStackTrace();
        }
        return departmentsList;
    }

    public Departments addDepartment(DepartmentDTO dto){
        Departments departments = new Departments();
        departments.setCreateAt(LocalDateTime.now());
        departments.setName(dto.getDepartmentName());
        departments.setDescription(dto.getDepartmentDescription());
        departments.setStatus(DepartmentStatus.valueOf(dto.getDepartmentStatus()));
        departmentRepository.save(departments);
        return departments;

    }
    public void changeStatus(Long id){

        Departments department =
                departmentRepository.findById(id)
                        .orElseThrow();

        if(department.getStatus()
                == DepartmentStatus.ACTIVE){

            department.setStatus(
                    DepartmentStatus.INACTIVE
            );

        }else{

            department.setStatus(
                    DepartmentStatus.ACTIVE
            );
        }

        departmentRepository.save(department);
    }
}
