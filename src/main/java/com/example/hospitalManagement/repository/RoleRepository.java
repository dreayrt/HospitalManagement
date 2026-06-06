package com.example.hospitalManagement.repository;

import com.example.hospitalManagement.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByNameIgnoreCase(String name);
}
