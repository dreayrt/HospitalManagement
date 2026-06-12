package com.example.hospitalManagement.kafka.producer;

import com.example.hospitalManagement.dto.AppointmentNotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    public void sendAppointment(AppointmentNotificationMessage message){
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            kafkaTemplate.send("appointment-notification", jsonMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//    public void sendCancellation(String message){
//        kafkaTemplate.send("cancellation-notification", message);
//    }
//    public void sendReminder(String message){
//        kafkaTemplate.send("reminder-notification", message);
//    }
}
