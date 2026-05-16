package com.example.hospitalManagement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "waiting_queues")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WaitingQueue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "queue_number", nullable = false)
    private Integer queueNumber;

    @Column(name = "is_priority", nullable = false)
    private Boolean isPriority = false;

    @Column(name = "joined_time", nullable = false)
    private LocalDateTime joinedTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueStatus status = QueueStatus.WAITING;

    public enum QueueStatus {
        WAITING,
        IN_PROGRESS,
        SKIPPED
    }
}