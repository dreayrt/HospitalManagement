package com.example.hospitalManagement.entity;

import com.example.hospitalManagement.entity.Enum.UserStatus;
import jakarta.persistence.*;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "specialization")
    private String specialization;
    @Column(name = "license_number")
    private String licenseNumber;
    @Column(name ="experience_years" )
    private Integer experienceYears;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus userStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonIgnoreProperties("doctors")
    private Departments department;

    @JoinColumn(name = "user_id")
    @OneToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties("doctor")
    private User user;

    @OneToMany(mappedBy = "doctor")
    @JsonIgnore
    private List<DoctorSchedules> doctorSchedules;

    @OneToMany(mappedBy = "doctor")
    @JsonIgnore
    private List<Appointments> appointments;

    @OneToMany(mappedBy = "doctor")
    @JsonIgnore
    private List<MedicalRecords> medicalRecords;

    @OneToMany(mappedBy = "doctor")
    @JsonIgnore
    private List<Prescription> prescriptions;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }


    public UserStatus getUserStatus() {
        return userStatus;
    }
    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public Departments getDepartment() {
        return department;
    }

    public void setDepartment(Departments department) {
        this.department = department;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<DoctorSchedules> getDoctorSchedules() {
        return doctorSchedules;
    }

    public void setDoctorSchedules(List<DoctorSchedules> doctorSchedules) {
        this.doctorSchedules = doctorSchedules;
    }

    public List<Appointments> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointments> appointments) {
        this.appointments = appointments;
    }

    public List<MedicalRecords> getMedicalRecords() {
        return medicalRecords;
    }

    public void setMedicalRecords(List<MedicalRecords> medicalRecords) {
        this.medicalRecords = medicalRecords;
    }

    public List<Prescription> getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(List<Prescription> prescriptions) {
        this.prescriptions = prescriptions;
    }
}