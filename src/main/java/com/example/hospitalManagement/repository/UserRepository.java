package com.example.hospitalManagement.repository;

import com.example.hospitalManagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String Phone);
    boolean existsByUserName(String userName);

    Optional<User> findByUserName(String userName);
    Optional<User> findById(Long id);

    @Query("""
            select u
            from User u
            left join u.doctor d
            where d is null
            order by u.fullName asc
            """)
    List<User> findUsersWithoutDoctorProfile();

    User findByEmail(String email);
}
