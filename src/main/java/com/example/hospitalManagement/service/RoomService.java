package com.example.hospitalManagement.service;

import com.example.hospitalManagement.entity.Room;
import com.example.hospitalManagement.repository.RoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoomService {
    @Autowired
    private RedisService redisService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RoomRepository roomRepository;

    public List<Room> getAllRooms() {
        String key="rooms";
        if(redisService.exists(key)){
            try{
                String json=(String) redisService.get(key);
                return objectMapper.readValue(json,objectMapper.getTypeFactory().constructCollectionType(List.class, Room.class));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        List<Room> rooms= roomRepository.findAll();
        try{
            String json= objectMapper.writeValueAsString(rooms);
            redisService.save(key,json,3600);
        }catch (Exception e){
            e.printStackTrace();
        }
        return  rooms;
    }
}
