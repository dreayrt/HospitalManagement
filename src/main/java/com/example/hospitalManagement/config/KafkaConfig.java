package com.example.hospitalManagement.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic appointmentTopic() {
        return TopicBuilder
                .name("appointment-notification")
                .build();
    }
    @Bean
    public NewTopic cancellationTopic() {
        return TopicBuilder
                .name("cancellation-notification")
                .build();
    }
    @Bean
    public NewTopic reminderTopic() {
        return TopicBuilder
                .name("reminder-notification")
                .build();
    }
}
