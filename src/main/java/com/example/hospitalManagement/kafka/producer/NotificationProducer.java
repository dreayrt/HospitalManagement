package com.example.hospitalManagement.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    public void sendAppointment(String message){
        kafkaTemplate.send("appointment-notification", message);
    }
    public void sendCancellation(String message){
        kafkaTemplate.send("cancellation-notification", message);
    }
    public void sendReminder(String message){
        kafkaTemplate.send("reminder-notification", message);
    }
}
