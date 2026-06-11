package com.example.hospitalManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    @Autowired
    private RedisTemplate redisTemplate;
    
    public void save(String key, String value, long timeout) {
        redisTemplate.opsForValue()
                .set(key,value,timeout, TimeUnit.SECONDS);
    }
    //get cache
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    //Delete cache
    public void remove(String key) {
        redisTemplate.delete(key);
    }
    //check exists
    public boolean exists(String key) {
        Boolean exists = redisTemplate.hasKey(key);
        if(exists != null && exists == true){
            return true;
        }
        return false;
    }
}
