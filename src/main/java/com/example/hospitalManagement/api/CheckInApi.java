package com.example.hospitalManagement.api;

import com.example.hospitalManagement.dto.CheckInDTO;
import com.example.hospitalManagement.entity.Enum.CheckInStatus;
import com.example.hospitalManagement.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;





@RestController
@RequestMapping("/api/checkins")
@CrossOrigin(origins = "*")
public class CheckInApi {

    @Autowired
    private CheckInService checkInService;

    @GetMapping("/statuses")
    public List<Map<String, String>> getStatuses() {
        return List.of(
                Map.of("value", CheckInStatus.WAITING.name(), "label", "Đang chờ"),
                Map.of("value", CheckInStatus.CHECKED_IN.name(), "label", "Đã vào khám"),
                Map.of("value", CheckInStatus.DONE.name(), "label", "Hoàn thành")
        );
    }

    @PostMapping
    public ResponseEntity<?> checkIn(@RequestBody Map<String, Object> body) {
        try {
            Long appointmentId = parseLong(body.get("appointmentId"));
            if (appointmentId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "appointmentId là bắt buộc."));
            }
            Integer priority = body.get("priority") != null ? parseInt(body.get("priority")) : 1;
            String notes = body.get("notes") != null ? body.get("notes").toString() : null;

            CheckInDTO result = checkInService.doCheckIn(appointmentId, priority, notes);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalStateException | java.util.NoSuchElementException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
    @GetMapping
    public ResponseEntity<Page<CheckInDTO>> getAllCheckIns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String search) {

        CheckInStatus checkInStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                checkInStatus = CheckInStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        Page<CheckInDTO> result = checkInService.findWithFilters(
                checkInStatus, fromDate, toDate, doctorId, search, page, size);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/stats/today")
    public ResponseEntity<Map<String, Long>> getTodayStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(checkInService.getStatsForDate(targetDate));
    }
    @PutMapping("/{id}/confirm-arrival")
    public ResponseEntity<?> confirmArrival(@PathVariable Long id) {
        try {
            CheckInDTO result = checkInService.confirmArrival(id);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException | java.util.NoSuchElementException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeCheckIn(@PathVariable Long id) {
        try {
            CheckInDTO result = checkInService.completeCheckIn(id);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException | java.util.NoSuchElementException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
    @PutMapping("/{id}/priority")
    public ResponseEntity<?> updatePriority(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            Integer priority = parseInt(body.get("priority"));
            if (priority == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "priority là bắt buộc."));
            }
            CheckInDTO result = checkInService.updatePriority(id, priority);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException | java.util.NoSuchElementException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
    private Long parseLong(Object val) {
        if (val == null) return null;
        try { return Long.parseLong(val.toString()); }
        catch (NumberFormatException e) { return null; }
    }

    private Integer parseInt(Object val) {
        if (val == null) return null;
        try { return Integer.parseInt(val.toString()); }
        catch (NumberFormatException e) { return null; }
    }
}
