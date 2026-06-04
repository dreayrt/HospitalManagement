package com.example.hospitalManagement.entity;

import com.example.hospitalManagement.entity.Enum.CheckInStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "check_ins")
public class CheckIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;
    @Column(name = "queue_number")
    private Integer queueNumber;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CheckInStatus status;
    @OneToOne
    @JoinColumn(name = "appointment_id")
    private Appointments appointments;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public Integer getQueueNumber() {
        return queueNumber;
    }

    public void setQueueNumber(Integer queueNumber) {
        this.queueNumber = queueNumber;
    }

    public CheckInStatus getStatus() {
        return status;
    }

    public void setStatus(CheckInStatus status) {
        this.status = status;
    }

    public Appointments getAppointments() {
        return appointments;
    }

    public void setAppointments(Appointments appointments) {
        this.appointments = appointments;
    }
}
