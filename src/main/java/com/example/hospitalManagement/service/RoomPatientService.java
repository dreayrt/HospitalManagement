package com.example.hospitalManagement.service;

import com.example.hospitalManagement.dto.AddPatientRoomRequest;
import com.example.hospitalManagement.entity.Patient;
import com.example.hospitalManagement.entity.Room;
import com.example.hospitalManagement.entity.RoomPatient;
import com.example.hospitalManagement.repository.PatientRepository;
import com.example.hospitalManagement.repository.RoomPatientRepository;
import com.example.hospitalManagement.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hospitalManagement.dto.PatientRoomDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoomPatientService {
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private RoomPatientRepository roomPatientRepository;
    @Autowired
    private RedisService redisService;
    @Transactional
    public String AddPatient(AddPatientRoomRequest addPatientRoomRequest) {
        Room room = roomRepository.findById(addPatientRoomRequest.getRoomId()).get();
        Patient patient =patientRepository.findById(addPatientRoomRequest.getPatientId()).get();
        RoomPatient roomPatient = new RoomPatient();
        roomPatient.setRoom(room);
        roomPatient.setPatient(patient);
        roomPatient.setCheckInTime(LocalDateTime.now());
        long quantityCurrent=roomPatientRepository.countByRoomIdAndCheckOutTimeIsNull(room.getId());
        if(quantityCurrent >= room.getMaxCapacity()){
            return "Phòng đã đầy, vui lòng chọn phòng khác!";
        }
        boolean exists = roomPatientRepository.existsByPatientIdAndCheckOutTimeIsNull(patient.getId());
        if(exists){
           return "Bệnh nhân đang ở phòng khác";
        }
        roomPatientRepository.save(roomPatient);
        redisService.remove("rooms");
        return "Successfully";
    }

    public List<PatientRoomDTO> getActivePatientsInRoom(long roomId) {
        List<RoomPatient> activePatients = roomPatientRepository.findByRoomIdAndCheckOutTimeIsNull(roomId);
        return activePatients.stream().map(rp -> {
            Patient p = rp.getPatient();
            String name = p.getUser() != null ? p.getUser().getFullName() : "Unknown";
            return new PatientRoomDTO(p.getId(), name, p.getPatientCode(), rp.getCheckInTime());
        }).collect(Collectors.toList());
    }

    @Transactional
    public String dischargePatient(long roomId, long patientId) {
        Optional<RoomPatient> optRp = roomPatientRepository.findByRoomIdAndPatientIdAndCheckOutTimeIsNull(roomId, patientId);
        if (optRp.isEmpty()) {
            return "Không tìm thấy bệnh nhân đang ở trong phòng này.";
        }
        RoomPatient rp = optRp.get();
        rp.setCheckOutTime(LocalDateTime.now());
        roomPatientRepository.save(rp);
        redisService.remove("rooms");
        return "Successfully";
    }
}
