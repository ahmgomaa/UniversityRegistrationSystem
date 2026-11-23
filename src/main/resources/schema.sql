CREATE TABLE departments (
    dept_code VARCHAR(10) PRIMARY KEY,
    dept_name VARCHAR(100) NOT NULL
);

CREATE TABLE users (
    user_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    phone VARCHAR(20),
    user_type ENUM('STUDENT','INSTRUCTOR','STAFF') NOT NULL
);

CREATE TABLE programs (
    program_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    dept_code VARCHAR(10) NOT NULL,
    total_credits INT NOT NULL,
    min_cgpa DOUBLE NOT NULL,
    elective_credits INT NOT NULL,
    max_credits_per_semester INT NOT NULL,
    probation_max_credits INT NOT NULL,
    probation_cgpa DOUBLE NOT NULL,
    number_of_levels INT NOT NULL,
    FOREIGN KEY (dept_code) REFERENCES departments(dept_code)
);

CREATE TABLE courses (
    course_code VARCHAR(10) PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL,
    credit_hours INT NOT NULL,
    dept_code VARCHAR(10) NOT NULL,
    FOREIGN KEY (dept_code) REFERENCES departments(dept_code)
);

CREATE TABLE course_prerequisites (
    course_code VARCHAR(10) NOT NULL,
    prerequisite_code VARCHAR(10) NOT NULL,
    PRIMARY KEY (course_code, prerequisite_code),
    FOREIGN KEY (course_code) REFERENCES courses(course_code),
    FOREIGN KEY (prerequisite_code) REFERENCES courses(course_code)
);

CREATE TABLE program_courses (
    program_id INT NOT NULL,
    course_code VARCHAR(10) NOT NULL,
    course_kind ENUM('REQUIRED','ELECTIVE') NOT NULL,
    PRIMARY KEY (program_id, course_code),
    FOREIGN KEY (program_id) REFERENCES programs(program_id),
    FOREIGN KEY (course_code) REFERENCES courses(course_code)
);

CREATE TABLE staff_members (
    user_id VARCHAR(20) PRIMARY KEY,
    dept_code VARCHAR(10) NOT NULL,
    roles VARCHAR(60) NOT NULL DEFAULT '',
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (dept_code) REFERENCES departments(dept_code)
);

CREATE TABLE students (
    user_id VARCHAR(20) PRIMARY KEY,
    student_number VARCHAR(20) NOT NULL UNIQUE,
    admission_year INT NOT NULL,
    academic_status ENUM('ACTIVE','PROBATION','SUSPENDED','GRADUATED') NOT NULL,
    program_id INT NOT NULL,
    advisor_id VARCHAR(20) NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (program_id) REFERENCES programs(program_id),
    FOREIGN KEY (advisor_id) REFERENCES users(user_id)
);

CREATE TABLE semesters (
    semester_id VARCHAR(20) PRIMARY KEY,
    term ENUM('SPRING','SUMMER','FALL') NOT NULL,
    year INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    withdrawal_deadline DATE NOT NULL,
    registration_start DATE NOT NULL,
    registration_end DATE NOT NULL
);

CREATE TABLE course_sections (
    section_id VARCHAR(30) PRIMARY KEY,
    course_code VARCHAR(10) NOT NULL,
    instructor_id VARCHAR(20) NOT NULL,
    semester_id VARCHAR(20) NOT NULL,
    section_no VARCHAR(5) NOT NULL,
    capacity INT NOT NULL,
    FOREIGN KEY (course_code) REFERENCES courses(course_code),
    FOREIGN KEY (instructor_id) REFERENCES users(user_id),
    FOREIGN KEY (semester_id) REFERENCES semesters(semester_id)
);

CREATE TABLE meeting_times (
    meeting_id INT PRIMARY KEY AUTO_INCREMENT,
    section_id VARCHAR(30) NOT NULL,
    meeting_day ENUM('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room VARCHAR(20) NOT NULL,
    FOREIGN KEY (section_id) REFERENCES course_sections(section_id)
);

CREATE TABLE enrollments (
    enrollment_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(20) NOT NULL,
    section_id VARCHAR(30) NOT NULL,
    status ENUM('ENROLLED','WITHDRAWN','COMPLETED') NOT NULL,
    score INT NULL,
    enrolled_on DATE NOT NULL,
    withdrawn_on DATE NULL,
    UNIQUE (user_id, section_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (section_id) REFERENCES course_sections(section_id)
);

CREATE TABLE notifications (
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    recipient_id VARCHAR(20) NOT NULL,
    message VARCHAR(255) NOT NULL,
    sent_at DATETIME NOT NULL,
    FOREIGN KEY (recipient_id) REFERENCES users(user_id)
);
