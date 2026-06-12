package com.example.hospitalManagement.service;

import com.example.hospitalManagement.entity.Doctor;
import com.example.hospitalManagement.repository.DoctorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DoctorServiceV2 {
    @Autowired
    private RedisService redisService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DoctorRepository doctorRepository;

    public List<Doctor> getAllDoctors() {
        String key= "doctors";
        try{
            if(redisService.exists(key)){
                String json= (String) redisService.get(key);
                return objectMapper.readValue(json,objectMapper.getTypeFactory().constructCollectionType(List.class, Doctor.class));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        List<Doctor> doctors=doctorRepository.findAll();
        try{
            String json= objectMapper.writeValueAsString(doctors);
            redisService.save(key,json,3600);
        }catch(Exception e){
            e.printStackTrace();
        }
        return doctors;
    }

    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id).orElse(null);
    }
}
