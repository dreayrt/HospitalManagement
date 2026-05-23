package com.example.hospitalManagement.repository;

import com.example.hospitalManagement.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room,Long> {
}
