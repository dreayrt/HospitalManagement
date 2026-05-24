CREATE TABLE IF NOT EXISTS specialties (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(255) NULL,
    active BIT NOT NULL DEFAULT 1,
    CONSTRAINT uk_specialties_code UNIQUE (code),
    CONSTRAINT uk_specialties_name UNIQUE (name)
);

ALTER TABLE doctors
    ADD CONSTRAINT uk_doctors_license_number UNIQUE (license_number);

CREATE INDEX idx_doctors_specialization ON doctors (specialization);
CREATE INDEX idx_doctor_schedules_doctor_date ON doctor_schedules (doctor_id, work_date, start_time);
