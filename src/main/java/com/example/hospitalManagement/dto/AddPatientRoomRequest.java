package com.example.hospitalManagement.dto;

public class AddPatientRoomRequest {
    private long RoomId;
    private long PatientId;

    public long getRoomId() {
        return RoomId;
    }

    public void setRoomId(long roomId) {
        RoomId = roomId;
    }

    public long getPatientId() {
        return PatientId;
    }

    public void setPatientId(long patientId) {
        PatientId = patientId;
    }
}
