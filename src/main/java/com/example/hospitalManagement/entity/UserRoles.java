package com.example.hospitalManagement.entity;

import jakarta.persistence.*;



@Entity
@Table(name = "user_roles")
public class UserRoles {
    @EmbeddedId
    private UserRoleId Id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    public UserRoleId getId() {
        return Id;
    }

    public void setId(UserRoleId id) {
        Id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

}
