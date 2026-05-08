CREATE DATABASE hospital_management_system;
USE hospital_management_system;

-- =========================
-- 1. USERS & SECURITY
-- =========================

CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY(user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY(role_id)
        REFERENCES roles(id)
        ON DELETE CASCADE
);

-- =========================
-- 2. DEPARTMENTS & ROOMS
-- =========================

CREATE TABLE departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    department_id BIGINT NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    room_type VARCHAR(50),
    status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE') DEFAULT 'AVAILABLE',

    CONSTRAINT fk_rooms_department
        FOREIGN KEY(department_id)
        REFERENCES departments(id)
);

-- =========================
-- 3. DOCTORS
-- =========================

CREATE TABLE doctors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    department_id BIGINT,
    specialization VARCHAR(100),
    license_number VARCHAR(50) UNIQUE,
    experience_years INT,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',

    CONSTRAINT fk_doctor_user
        FOREIGN KEY(user_id)
        REFERENCES users(id),

    CONSTRAINT fk_doctor_department
        FOREIGN KEY(department_id)
        REFERENCES departments(id)
);

CREATE TABLE doctor_schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    doctor_id BIGINT NOT NULL,
    work_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM('AVAILABLE', 'UNAVAILABLE') DEFAULT 'AVAILABLE',

    CONSTRAINT fk_schedule_doctor
        FOREIGN KEY(doctor_id)
        REFERENCES doctors(id)
);

-- =========================
-- 4. PATIENTS
-- =========================

CREATE TABLE patients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE,
    patient_code VARCHAR(50) UNIQUE NOT NULL,
    dob DATE,
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    address TEXT,
    insurance_number VARCHAR(50),
    blood_type VARCHAR(10),
    emergency_contact VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_patient_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
);

CREATE TABLE patient_health_metrics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,
    height DECIMAL(5,2),
    weight DECIMAL(5,2),
    blood_pressure VARCHAR(20),
    heart_rate INT,
    temperature DECIMAL(4,2),
    recorded_by BIGINT,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_metric_patient
        FOREIGN KEY(patient_id)
        REFERENCES patients(id),

    CONSTRAINT fk_metric_user
        FOREIGN KEY(recorded_by)
        REFERENCES users(id)
);

-- =========================
-- 5. APPOINTMENTS
-- =========================

CREATE TABLE appointments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    reason TEXT,
    status ENUM(
        'PENDING',
        'CONFIRMED',
        'IN_PROGRESS',
        'COMPLETED',
        'CANCELLED'
    ) DEFAULT 'PENDING',

    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_appointment_patient
        FOREIGN KEY(patient_id)
        REFERENCES patients(id),

    CONSTRAINT fk_appointment_doctor
        FOREIGN KEY(doctor_id)
        REFERENCES doctors(id),

    CONSTRAINT fk_appointment_created_by
        FOREIGN KEY(created_by)
        REFERENCES users(id)
);

CREATE TABLE check_ins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_id BIGINT NOT NULL,
    check_in_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    queue_number INT,
    status ENUM('WAITING', 'CHECKED_IN', 'DONE') DEFAULT 'WAITING',

    CONSTRAINT fk_checkin_appointment
        FOREIGN KEY(appointment_id)
        REFERENCES appointments(id)
);

-- =========================
-- 6. MEDICAL RECORDS
-- =========================

CREATE TABLE medical_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,

    diagnosis TEXT,
    symptoms TEXT,
    treatment_plan TEXT,
    notes TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_medical_appointment
        FOREIGN KEY(appointment_id)
        REFERENCES appointments(id),

    CONSTRAINT fk_medical_doctor
        FOREIGN KEY(doctor_id)
        REFERENCES doctors(id),

    CONSTRAINT fk_medical_patient
        FOREIGN KEY(patient_id)
        REFERENCES patients(id)
);

-- =========================
-- 7. PRESCRIPTIONS
-- =========================

CREATE TABLE prescriptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    medical_record_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_prescription_medical
        FOREIGN KEY(medical_record_id)
        REFERENCES medical_records(id),

    CONSTRAINT fk_prescription_doctor
        FOREIGN KEY(doctor_id)
        REFERENCES doctors(id),

    CONSTRAINT fk_prescription_patient
        FOREIGN KEY(patient_id)
        REFERENCES patients(id)
);

CREATE TABLE prescription_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    prescription_id BIGINT NOT NULL,
    medicine_name VARCHAR(100) NOT NULL,
    dosage VARCHAR(100),
    frequency VARCHAR(100),
    duration VARCHAR(100),
    instruction TEXT,

    CONSTRAINT fk_prescription_detail
        FOREIGN KEY(prescription_id)
        REFERENCES prescriptions(id)
        ON DELETE CASCADE
);

-- =========================
-- 8. INVOICES & PAYMENTS
-- =========================

CREATE TABLE invoices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,
    appointment_id BIGINT,
    invoice_code VARCHAR(50) UNIQUE NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,

    status ENUM(
        'PENDING',
        'PAID',
        'CANCELLED',
        'REFUNDED'
    ) DEFAULT 'PENDING',

    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_invoice_patient
        FOREIGN KEY(patient_id)
        REFERENCES patients(id),

    CONSTRAINT fk_invoice_appointment
        FOREIGN KEY(appointment_id)
        REFERENCES appointments(id)
);

CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoice_id BIGINT NOT NULL,
    payment_method VARCHAR(50),
    amount DECIMAL(12,2),
    paid_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    transaction_code VARCHAR(100),

    status ENUM(
        'PENDING',
        'SUCCESS',
        'FAILED'
    ) DEFAULT 'PENDING',

    CONSTRAINT fk_payment_invoice
        FOREIGN KEY(invoice_id)
        REFERENCES invoices(id)
);

-- =========================
-- 9. NOTIFICATIONS
-- =========================

CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(50),
    title VARCHAR(255),
    message TEXT,

    status ENUM(
        'PENDING',
        'SENT',
        'FAILED'
    ) DEFAULT 'PENDING',

    sent_at TIMESTAMP NULL,

    CONSTRAINT fk_notification_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
);

-- =========================
-- 10. AUDIT LOGS
-- =========================

CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    action VARCHAR(255),
    entity_name VARCHAR(100),
    entity_id BIGINT,
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
);

-- =========================
-- INSERT DEFAULT ROLES
-- =========================

INSERT INTO roles(name, description)
VALUES
('ADMIN', 'System Administrator'),
('DOCTOR', 'Doctor Role'),
('NURSE', 'Nurse Role'),
('RECEPTIONIST', 'Receptionist Role'),
('PATIENT', 'Patient Role');

INSERT INTO users(username, email, password, full_name, phone, status)
VALUES
('admin1', 'admin1@gmail.com', '123456', 'Nguyen Van Admin', '0901000001', 'ACTIVE'),
('doctor1', 'doctor1@gmail.com', '123456', 'Tran Minh Doctor', '0901000002', 'ACTIVE'),
('doctor2', 'doctor2@gmail.com', '123456', 'Le Hoang Doctor', '0901000003', 'ACTIVE'),
('doctor3', 'doctor3@gmail.com', '123456', 'Doctor Three', '0901000011', 'ACTIVE'),
('doctor4', 'doctor4@gmail.com', '123456', 'Doctor Four', '0901000012', 'ACTIVE'),
('doctor5', 'doctor5@gmail.com', '123456', 'Doctor Five', '0901000013', 'ACTIVE'),
('nurse1', 'nurse1@gmail.com', '123456', 'Pham Thi Nurse', '0901000004', 'ACTIVE'),
('reception1', 'reception1@gmail.com', '123456', 'Vo Receptionist', '0901000005', 'ACTIVE'),

('patient1', 'patient1@gmail.com', '123456', 'Nguyen Van A', '0901000006', 'ACTIVE'),
('patient2', 'patient2@gmail.com', '123456', 'Tran Thi B', '0901000007', 'ACTIVE'),
('patient3', 'patient3@gmail.com', '123456', 'Le Van C', '0901000008', 'ACTIVE'),
('patient4', 'patient4@gmail.com', '123456', 'Pham Thi D', '0901000009', 'ACTIVE'),
('patient5', 'patient5@gmail.com', '123456', 'Hoang Van E', '0901000010', 'ACTIVE');

-- =========================
-- USER ROLES
-- =========================

INSERT INTO user_roles(user_id, role_id)
VALUES
(1, 1),

(2, 2),
(3, 2),

(4, 3),

(5, 4),

(6, 5),
(7, 5),
(8, 5),
(9, 5),
(10, 5),
(11, 2),
(12, 2),
(13, 2);

-- =========================
-- DEPARTMENTS
-- =========================

INSERT INTO departments(name, description, status)
VALUES
('Cardiology', 'Heart Department', 'ACTIVE'),
('Neurology', 'Brain and Nervous System', 'ACTIVE'),
('Pediatrics', 'Children Department', 'ACTIVE'),
('Orthopedics', 'Bone Department', 'ACTIVE'),
('Dermatology', 'Skin Department', 'ACTIVE');

-- =========================
-- ROOMS
-- =========================

INSERT INTO rooms(department_id, room_number, room_type, status)
VALUES
(1, 'C101', 'EXAMINATION', 'AVAILABLE'),
(2, 'N201', 'EXAMINATION', 'AVAILABLE'),
(3, 'P301', 'EXAMINATION', 'AVAILABLE'),
(4, 'O401', 'SURGERY', 'AVAILABLE'),
(5, 'D501', 'CONSULTATION', 'AVAILABLE');

-- =========================
-- DOCTORS
-- =========================

INSERT INTO doctors(user_id, department_id, specialization, license_number, experience_years, status)
VALUES
(2, 1, 'Cardiologist', 'LIC001', 10, 'ACTIVE'),
(3, 2, 'Neurologist', 'LIC002', 7, 'ACTIVE'),
(11, 3, 'Pediatric Specialist', 'LIC003', 5, 'ACTIVE'),
(12, 4, 'Orthopedic Specialist', 'LIC004', 8, 'ACTIVE'),
(13, 5, 'Dermatologist', 'LIC005', 6, 'ACTIVE');

-- =========================
-- DOCTOR SCHEDULES
-- =========================

INSERT INTO doctor_schedules(doctor_id, work_date, start_time, end_time, status)
VALUES
(1, '2026-05-10', '08:00:00', '12:00:00', 'AVAILABLE'),
(2, '2026-05-10', '13:00:00', '17:00:00', 'AVAILABLE'),
(3, '2026-05-11', '08:00:00', '12:00:00', 'AVAILABLE'),
(4, '2026-05-11', '13:00:00', '17:00:00', 'AVAILABLE'),
(5, '2026-05-12', '08:00:00', '12:00:00', 'AVAILABLE');

-- =========================
-- PATIENTS
-- =========================

INSERT INTO patients(
    user_id,
    patient_code,
    dob,
    gender,
    address,
    insurance_number,
    blood_type,
    emergency_contact
)
VALUES
(6, 'P001', '2000-01-01', 'MALE', 'Ho Chi Minh City', 'INS001', 'A+', '0902000001'),
(7, 'P002', '1999-02-10', 'FEMALE', 'Da Nang', 'INS002', 'B+', '0902000002'),
(8, 'P003', '2001-03-15', 'MALE', 'Can Tho', 'INS003', 'O+', '0902000003'),
(9, 'P004', '1998-07-20', 'FEMALE', 'Ha Noi', 'INS004', 'AB+', '0902000004'),
(10, 'P005', '2002-09-25', 'MALE', 'Hue', 'INS005', 'A-', '0902000005');

-- =========================
-- PATIENT HEALTH METRICS
-- =========================

INSERT INTO patient_health_metrics(
    patient_id,
    height,
    weight,
    blood_pressure,
    heart_rate,
    temperature,
    recorded_by
)
VALUES
(1, 170.5, 65.0, '120/80', 72, 36.5, 4),
(2, 160.0, 50.0, '110/70', 75, 36.7, 4),
(3, 175.0, 70.5, '125/85', 80, 37.0, 4),
(4, 168.0, 58.0, '118/78', 73, 36.6, 4),
(5, 180.0, 75.0, '130/90', 85, 37.2, 4);

-- =========================
-- APPOINTMENTS
-- =========================

INSERT INTO appointments(
    patient_id,
    doctor_id,
    appointment_date,
    appointment_time,
    reason,
    status,
    created_by
)
VALUES
(1, 1, '2026-05-10', '08:30:00', 'Heart Checkup', 'CONFIRMED', 5),
(2, 2, '2026-05-10', '14:00:00', 'Headache', 'PENDING', 5),
(3, 3, '2026-05-11', '09:00:00', 'Child Fever', 'CONFIRMED', 5),
(4, 4, '2026-05-11', '15:00:00', 'Bone Pain', 'COMPLETED', 5),
(5, 5, '2026-05-12', '10:00:00', 'Skin Allergy', 'CANCELLED', 5);

-- =========================
-- CHECK INS
-- =========================

INSERT INTO check_ins(
    appointment_id,
    queue_number,
    status
)
VALUES
(1, 1, 'CHECKED_IN'),
(2, 2, 'WAITING'),
(3, 3, 'CHECKED_IN'),
(4, 4, 'DONE'),
(5, 5, 'WAITING');

-- =========================
-- MEDICAL RECORDS
-- =========================

INSERT INTO medical_records(
    appointment_id,
    doctor_id,
    patient_id,
    diagnosis,
    symptoms,
    treatment_plan,
    notes
)
VALUES
(1, 1, 1, 'Mild Heart Disease', 'Chest Pain', 'Medication and Rest', 'Need follow-up'),
(2, 2, 2, 'Migraine', 'Headache', 'Painkiller', 'Reduce stress'),
(3, 3, 3, 'Flu', 'Fever and Cough', 'Medicine for 5 days', 'Drink more water'),
(4, 4, 4, 'Bone Fracture', 'Leg Pain', 'Surgery', 'Hospitalized'),
(5, 5, 5, 'Skin Allergy', 'Itching', 'Skin Cream', 'Avoid seafood');

-- =========================
-- PRESCRIPTIONS
-- =========================

INSERT INTO prescriptions(
    medical_record_id,
    doctor_id,
    patient_id
)
VALUES
(1, 1, 1),
(2, 2, 2),
(3, 3, 3),
(4, 4, 4),
(5, 5, 5);

-- =========================
-- PRESCRIPTION DETAILS
-- =========================

INSERT INTO prescription_details(
    prescription_id,
    medicine_name,
    dosage,
    frequency,
    duration,
    instruction
)
VALUES
(1, 'Aspirin', '1 pill', '2 times/day', '7 days', 'After meal'),
(2, 'Panadol', '1 pill', '3 times/day', '5 days', 'Drink water'),
(3, 'Paracetamol', '1 pill', '2 times/day', '5 days', 'Rest at home'),
(4, 'Vitamin C', '2 pills', '1 time/day', '10 days', 'Morning'),
(5, 'Skin Cream', 'Apply', '2 times/day', '14 days', 'External use');

-- =========================
-- INVOICES
-- =========================

INSERT INTO invoices(
    patient_id,
    appointment_id,
    invoice_code,
    total_amount,
    status
)
VALUES
(1, 1, 'INV001', 100.00, 'PAID'),
(2, 2, 'INV002', 150.00, 'PENDING'),
(3, 3, 'INV003', 120.00, 'PAID'),
(4, 4, 'INV004', 500.00, 'PAID'),
(5, 5, 'INV005', 80.00, 'CANCELLED');

-- =========================
-- PAYMENTS
-- =========================

INSERT INTO payments(
    invoice_id,
    payment_method,
    amount,
    transaction_code,
    status
)
VALUES
(1, 'CASH', 100.00, 'TXN001', 'SUCCESS'),
(2, 'BANKING', 150.00, 'TXN002', 'PENDING'),
(3, 'CREDIT_CARD', 120.00, 'TXN003', 'SUCCESS'),
(4, 'CASH', 500.00, 'TXN004', 'SUCCESS'),
(5, 'BANKING', 80.00, 'TXN005', 'FAILED');

-- =========================
-- NOTIFICATIONS
-- =========================

INSERT INTO notifications(
    user_id,
    type,
    title,
    message,
    status
)
VALUES
(6, 'EMAIL', 'Appointment Confirmed', 'Your appointment has been confirmed', 'SENT'),
(7, 'EMAIL', 'Appointment Reminder', 'Reminder for tomorrow appointment', 'SENT'),
(8, 'EMAIL', 'Invoice Created', 'Your invoice is available', 'PENDING'),
(9, 'EMAIL', 'Appointment Cancelled', 'Your appointment has been cancelled', 'SENT'),
(10, 'EMAIL', 'Prescription Ready', 'Your prescription is ready', 'SENT');

-- =========================
-- AUDIT LOGS
-- =========================

INSERT INTO audit_logs(
    user_id,
    action,
    entity_name,
    entity_id,
    old_value,
    new_value
)
VALUES
(1, 'CREATE', 'PATIENT', 1, NULL, 'Patient Created'),
(1, 'UPDATE', 'DOCTOR', 2, 'Old Schedule', 'New Schedule'),
(5, 'CREATE', 'APPOINTMENT', 3, NULL, 'Appointment Created'),
(2, 'UPDATE', 'MEDICAL_RECORD', 1, 'Old Diagnosis', 'New Diagnosis'),
(1, 'DELETE', 'INVOICE', 5, 'Invoice Exists', 'Invoice Deleted');