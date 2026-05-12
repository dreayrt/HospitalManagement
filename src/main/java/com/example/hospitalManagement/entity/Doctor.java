package com.example.hospitalManagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(length = 100)
    private String specialization;

    @Column(name = "license_number", length = 50, unique = true)
    private String licenseNumber;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('ACTIVE','INACTIVE')")
    private Status status = Status.ACTIVE;

    public enum Status {
        ACTIVE, INACTIVE
    }
}