package com.example.hospitalManagement.entity;

import com.example.hospitalManagement.entity.Enum.RoomStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "rooms")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "room_code")
    private String roomCode;
    @Column(name = "room_name")
    private String roomName;
    @Column(name = "description")
    private String description;
    @Column(name = "max_capacity")
    private Integer maxCapacity;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RoomStatus roomStatus;
    @Column(name = "created_at")
    private LocalDateTime createAt;
    @OneToMany(mappedBy = "room")
    private List<RoomPatient>  roomPatients;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public RoomStatus getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(RoomStatus roomStatus) {
        this.roomStatus = roomStatus;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public List<RoomPatient> getRoomPatients() {
        return roomPatients;
    }

    public void setRoomPatients(List<RoomPatient> roomPatients) {
        this.roomPatients = roomPatients;
    }

    @Transient
    public int getCurrentPatientCount() {
        if (roomPatients == null) return 0;
        int count = 0;
        for (RoomPatient rp : roomPatients) {
            if (rp.getCheckOutTime() == null) {
                count++;
            }
        }
        return count;
    }
}
