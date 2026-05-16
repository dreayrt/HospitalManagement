package com.example.hospitalManagement.repository;

import com.example.hospitalManagement.entity.User;
import com.example.hospitalManagement.entity.UserRoles;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface userRepository extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String Phone);
    boolean existsByUserName(String userName);

    Optional<User> findByUserName(String userName);
}
