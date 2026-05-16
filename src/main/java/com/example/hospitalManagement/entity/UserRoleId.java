package com.example.hospitalManagement.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class UserRoleId implements Serializable {
    private Long userId;
    private Long roleId;
}
