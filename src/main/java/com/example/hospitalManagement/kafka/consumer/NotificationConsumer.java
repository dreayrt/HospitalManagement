package com.example.hospitalManagement.kafka.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @KafkaListener(topics = "appointment-notification",groupId = "notification-group")
    public void consumerAppointment(String message){
        //todo
        System.out.println("Appointment: "+message);
    }

    @KafkaListener(topics = "cancellation-notification",groupId = "notification-group")
    public void consumerCancellation(String message){
        //todo
        System.out.println("Cancellation: "+message);
    }
    @KafkaListener(topics = "reminder-notification",groupId = "notification-group")
    public void consumerReminder(String message){
        System.out.println("Reminder: "+message);
    }
}
