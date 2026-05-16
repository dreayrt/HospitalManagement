package com.example.hospitalManagement.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    /**
     * Create Kafka topic for appointment created events
     */
    @Bean
    public NewTopic appointmentCreatedTopic() {
        return TopicBuilder.name("appointment-created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Create Kafka topic for appointment cancelled events
     */
    @Bean
    public NewTopic appointmentCancelledTopic() {
        return TopicBuilder.name("appointment-cancelled")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Create Kafka topic for appointment rescheduled events
     */
    @Bean
    public NewTopic appointmentRescheduledTopic() {
        return TopicBuilder.name("appointment-rescheduled")
                .partitions(3)
                .replicas(1)
                .build();
    }
}

