package com.example.hospitalManagement.entity;

import com.example.hospitalManagement.entity.Enum.UserStatus;
import jakarta.persistence.*;

import java.util.List;

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
    private Departments department;

    @JoinColumn(name = "user_id")
    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    @OneToMany(mappedBy = "doctor")
    private List<DoctorSchedules> DoctorSchedules;

    @OneToMany(mappedBy = "doctor")
    private List<Appointments> Appointments;

    @OneToMany(mappedBy = "doctor")
    private List<MedicalRecords> MedicalRecords;

    @OneToMany(mappedBy = "doctor")
    private List<Prescription>  Prescriptions;

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
        return DoctorSchedules;
    }

    public void setDoctorSchedules(List<DoctorSchedules> doctorSchedules) {
        DoctorSchedules = doctorSchedules;
    }

    public List<Appointments> getAppointments() {
        return Appointments;
    }

    public void setAppointments(List<Appointments> appointments) {
        Appointments = appointments;
    }

    public List<MedicalRecords> getMedicalRecords() {
        return MedicalRecords;
    }

    public void setMedicalRecords(List<MedicalRecords> medicalRecords) {
        MedicalRecords = medicalRecords;
    }

    public List<Prescription> getPrescriptions() {
        return Prescriptions;
    }

    public void setPrescriptions(List<Prescription> prescriptions) {
        Prescriptions = prescriptions;
    }
}