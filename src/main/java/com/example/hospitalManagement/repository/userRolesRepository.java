package com.example.hospitalManagement.repository;

import com.example.hospitalManagement.entity.UserRoles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface userRolesRepository extends JpaRepository<UserRoles, Integer> {
    List<UserRoles> findAllByUser_Id(Integer id);
}
