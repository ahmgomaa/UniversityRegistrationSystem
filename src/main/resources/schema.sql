-- University Registration System — MySQL schema
CREATE DATABASE IF NOT EXISTS university_registration
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE university_registration;

DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS meeting_times;
DROP TABLE IF EXISTS course_sections;
DROP TABLE IF EXISTS registration_periods;
DROP TABLE IF EXISTS semesters;
DROP TABLE IF EXISTS program_courses;
DROP TABLE IF EXISTS course_prerequisites;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS programs;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS academic_staff_roles;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS users;

CREATE TABLE departments (
    dept_code VARCHAR(10)  NOT NULL,
    dept_name VARCHAR(120) NOT NULL,
    PRIMARY KEY (dept_code)
);

CREATE TABLE users (
    user_id       VARCHAR(36)  NOT NULL,
    user_type     ENUM('STUDENT','INSTRUCTOR','ACADEMIC_STAFF') NOT NULL,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(120) NOT NULL,
    password_hash VARCHAR(64)  NOT NULL,
    phone_number  VARCHAR(30),
    dept_code     VARCHAR(10)  NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_users_email (email),
    CONSTRAINT fk_users_dept FOREIGN KEY (dept_code) REFERENCES departments (dept_code)
);

CREATE TABLE students (
    user_id         VARCHAR(36) NOT NULL,
    student_number  VARCHAR(20) NOT NULL,
    admission_year  INT         NOT NULL,
    academic_status ENUM('ACTIVE','PROBATION','SUSPENDED','GRADUATED') NOT NULL DEFAULT 'ACTIVE',
    program_id      INT         NOT NULL,
    advisor_id      VARCHAR(36) NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_students_number (student_number),
    CONSTRAINT fk_students_user   FOREIGN KEY (user_id)    REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_students_advisor FOREIGN KEY (advisor_id) REFERENCES users (user_id) ON DELETE SET NULL
);

CREATE TABLE academic_staff_roles (
    user_id VARCHAR(36) NOT NULL,
    role    ENUM('ADVISOR','REGISTRAR') NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_staff_roles_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE programs (
    program_id               INT AUTO_INCREMENT PRIMARY KEY,
    name                     VARCHAR(120) NOT NULL,
    dept_code                VARCHAR(10)  NOT NULL,
    total_credits_required   INT    NOT NULL,
    minimum_cgpa             DOUBLE NOT NULL,
    elective_credits         INT    NOT NULL,
    max_credits_per_semester INT    NOT NULL,
    probation_max_credits    INT    NOT NULL,
    probation_cgpa           DOUBLE NOT NULL,
    number_of_levels         INT    NOT NULL,
    CONSTRAINT fk_programs_dept FOREIGN KEY (dept_code) REFERENCES departments (dept_code)
);

CREATE TABLE courses (
    course_code VARCHAR(10)  NOT NULL,
    course_name VARCHAR(150) NOT NULL,
    credit_hours INT         NOT NULL,
    dept_code   VARCHAR(10)  NOT NULL,
    PRIMARY KEY (course_code),
    CONSTRAINT fk_courses_dept FOREIGN KEY (dept_code) REFERENCES departments (dept_code)
);

CREATE TABLE course_prerequisites (
    course_code  VARCHAR(10) NOT NULL,
    prereq_code  VARCHAR(10) NOT NULL,
    PRIMARY KEY (course_code, prereq_code),
    CONSTRAINT fk_prereq_course FOREIGN KEY (course_code)  REFERENCES courses (course_code) ON DELETE CASCADE,
    CONSTRAINT fk_prereq_prereq FOREIGN KEY (prereq_code)  REFERENCES courses (course_code)
);

CREATE TABLE program_courses (
    program_id  INT         NOT NULL,
    course_code VARCHAR(10) NOT NULL,
    kind        ENUM('REQUIRED','ELECTIVE') NOT NULL,
    PRIMARY KEY (program_id, course_code),
    CONSTRAINT fk_pc_program FOREIGN KEY (program_id)   REFERENCES programs (program_id) ON DELETE CASCADE,
    CONSTRAINT fk_pc_course  FOREIGN KEY (course_code)  REFERENCES courses (course_code)
);

CREATE TABLE semesters (
    semester_id        VARCHAR(20) NOT NULL,   -- e.g. FALL-2026
    term               ENUM('SPRING','SUMMER','FALL') NOT NULL,
    year               INT  NOT NULL,
    start_date         DATE NOT NULL,
    end_date           DATE NOT NULL,
    withdrawal_deadline DATE NOT NULL,
    PRIMARY KEY (semester_id)
);

CREATE TABLE registration_periods (
    semester_id VARCHAR(20) NOT NULL,
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    PRIMARY KEY (semester_id),
    CONSTRAINT fk_regperiod_semester FOREIGN KEY (semester_id) REFERENCES semesters (semester_id) ON DELETE CASCADE
);

CREATE TABLE course_sections (
    section_id   VARCHAR(36) NOT NULL,
    course_code  VARCHAR(10) NOT NULL,
    instructor_id VARCHAR(36) NOT NULL,
    semester_id  VARCHAR(20) NOT NULL,
    section_no   VARCHAR(10) NOT NULL,   -- display number, e.g. "01"
    capacity     INT NOT NULL,
    PRIMARY KEY (section_id),
    UNIQUE KEY uq_section (course_code, semester_id, section_no),
    CONSTRAINT fk_sections_course     FOREIGN KEY (course_code)   REFERENCES courses (course_code),
    CONSTRAINT fk_sections_instructor FOREIGN KEY (instructor_id) REFERENCES users (user_id),
    CONSTRAINT fk_sections_semester   FOREIGN KEY (semester_id)   REFERENCES semesters (semester_id) ON DELETE CASCADE
);

CREATE TABLE meeting_times (
    meeting_id INT AUTO_INCREMENT PRIMARY KEY,
    section_id VARCHAR(36) NOT NULL,
    day        ENUM('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') NOT NULL,
    start_time TIME NOT NULL,
    end_time   TIME NOT NULL,
    room       VARCHAR(40) NOT NULL,
    CONSTRAINT fk_meetings_section FOREIGN KEY (section_id) REFERENCES course_sections (section_id) ON DELETE CASCADE
);

CREATE TABLE enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id    VARCHAR(36) NOT NULL,
    section_id    VARCHAR(36) NOT NULL,
    status        ENUM('ENROLLED','WITHDRAWN','COMPLETED') NOT NULL DEFAULT 'ENROLLED',
    score         DOUBLE NULL,
    enrolled_on   DATE NOT NULL,
    withdrawn_on  DATE NULL,
    UNIQUE KEY uq_enrollment (student_id, section_id),
    CONSTRAINT fk_enroll_student FOREIGN KEY (student_id)  REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_enroll_section FOREIGN KEY (section_id)  REFERENCES course_sections (section_id) ON DELETE CASCADE
);

CREATE TABLE notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    recipient_id    VARCHAR(36) NOT NULL,
    message         VARCHAR(500) NOT NULL,
    sent_at         DATETIME NOT NULL,
    CONSTRAINT fk_notif_recipient FOREIGN KEY (recipient_id) REFERENCES users (user_id) ON DELETE CASCADE
);
