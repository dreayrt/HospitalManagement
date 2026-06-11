package com.example.hospitalManagement.api;

import com.example.hospitalManagement.dto.AddPatientRoomRequest;
import com.example.hospitalManagement.service.RoomPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.hospitalManagement.dto.PatientRoomDTO;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AddPatientRoomApi {
    @Autowired
    private RoomPatientService roomPatientService;
    @Autowired
    private com.example.hospitalManagement.service.RedisService redisService;

    @GetMapping("/clear-cache")
    public ResponseEntity<?> clearCache() {
        redisService.remove("rooms");
        return ResponseEntity.ok("Đã xoá Cache Redis thành công!");
    }

    @PostMapping("/patientRoom")
    public ResponseEntity<?> addPatientRoom(@RequestBody AddPatientRoomRequest addPatientRoomRequest){
       String Result= roomPatientService.AddPatient(addPatientRoomRequest);
        if(!Result.equals("Successfully")){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result);
        }
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/patientRoom/{roomId}/patients")
    public ResponseEntity<List<PatientRoomDTO>> getActivePatientsInRoom(@PathVariable("roomId") long roomId) {
        return ResponseEntity.ok(roomPatientService.getActivePatientsInRoom(roomId));
    }

    @PutMapping("/patientRoom/{roomId}/discharge/{patientId}")
    public ResponseEntity<?> dischargePatient(@PathVariable("roomId") long roomId, @PathVariable("patientId") long patientId) {
        String result = roomPatientService.dischargePatient(roomId, patientId);
        if (!result.equals("Successfully")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        return ResponseEntity.ok("OK");
    }
}
