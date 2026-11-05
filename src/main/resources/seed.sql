-- University Registration System — demo data
USE university_registration;

-- All demo users have password: pass123
SET @pw := '9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c';

-- Departments ----------------------------------------------------------------
INSERT INTO departments (dept_code, dept_name) VALUES
    ('CS',  'Computer Science'),
    ('MATH','Mathematics');

-- Programs -------------------------------------------------------------------
INSERT INTO programs (program_id, name, dept_code, total_credits_required, minimum_cgpa,
                      elective_credits, max_credits_per_semester, probation_max_credits,
                      probation_cgpa, number_of_levels) VALUES
    (1, 'BSc Computer Science',      'CS', 120, 2.00, 12, 18, 12, 1.80, 4),
    (2, 'Diploma in CS Foundations', 'CS', 15,  2.00, 3,  9,  6,  1.80, 1);

-- Courses --------------------------------------------------------------------
INSERT INTO courses (course_code, course_name, credit_hours, dept_code) VALUES
    ('MATH101', 'Calculus I',            3, 'MATH'),
    ('CS101',   'Intro to Programming',  3, 'CS'),
    ('CS102',   'Object-Oriented Programming', 3, 'CS'),
    ('CS201',   'Data Structures',       3, 'CS'),
    ('CS301',   'Algorithms',            3, 'CS'),
    ('EE201',   'Digital Logic',         3, 'CS'),
    ('HUM101',  'Technical Writing',     3, 'CS');

INSERT INTO course_prerequisites (course_code, prereq_code) VALUES
    ('CS102', 'CS101'),
    ('CS201', 'CS102'),
    ('CS301', 'CS201');

INSERT INTO program_courses (program_id, course_code, kind) VALUES
    (1, 'MATH101', 'REQUIRED'), (1, 'CS101', 'REQUIRED'), (1, 'CS102', 'REQUIRED'),
    (1, 'CS201',  'REQUIRED'), (1, 'CS301', 'REQUIRED'),
    (1, 'HUM101', 'ELECTIVE'), (1, 'EE201', 'ELECTIVE'),
    (2, 'MATH101', 'REQUIRED'), (2, 'CS101', 'REQUIRED'), (2, 'CS102', 'REQUIRED'),
    (2, 'CS201',  'REQUIRED'), (2, 'HUM101', 'ELECTIVE');

-- Users ----------------------------------------------------------------------
-- Instructors
INSERT INTO users (user_id, user_type, name, email, password_hash, phone_number, dept_code) VALUES
    ('inst-omar',  'INSTRUCTOR',     'Omar Khaled',  'omar.k@university.edu',  @pw, '+20-100-111-0001', 'CS'),
    ('inst-laila', 'INSTRUCTOR',     'Laila Hassan', 'laila.h@university.edu', @pw, '+20-100-111-0002', 'CS'),
    ('inst-yousef','INSTRUCTOR',     'Yousef Adel',  'yousef.a@university.edu',@pw, '+20-100-111-0003', 'CS'),
    ('staff-mona', 'ACADEMIC_STAFF', 'Mona Sabet',   'mona.s@university.edu', @pw, '+20-100-111-0004', 'CS');

INSERT INTO academic_staff_roles (user_id, role) VALUES
    ('staff-mona', 'ADVISOR'),
    ('staff-mona', 'REGISTRAR');

-- Students
INSERT INTO users (user_id, user_type, name, email, password_hash, phone_number) VALUES
    ('stu-ahmed', 'STUDENT', 'Ahmed Samir',  'ahmed.samir@university.edu',  @pw, '+20-100-222-0001'),
    ('stu-sara',  'STUDENT', 'Sara Magdy',   'sara.magdy@university.edu',   @pw, '+20-100-222-0002'),
    ('stu-nour',  'STUDENT', 'Nour El-Sayed','nour.elsayed@university.edu', @pw, '+20-100-222-0003'),
    ('stu-khalid','STUDENT', 'Khalid Fathi', 'khalid.fathi@university.edu', @pw, '+20-100-222-0004');

INSERT INTO students (user_id, student_number, admission_year, academic_status, program_id, advisor_id) VALUES
    ('stu-ahmed', 'S-2023-001', 2023, 'ACTIVE',    1, 'staff-mona'),
    ('stu-sara',  'S-2024-002', 2024, 'PROBATION', 1, 'staff-mona'),
    ('stu-nour',  'S-2025-003', 2025, 'ACTIVE',    1, 'staff-mona'),
    ('stu-khalid','S-2022-004', 2022, 'ACTIVE',    2, 'staff-mona');

-- Semesters ------------------------------------------------------------------
-- SPRING-2026 (finished) and SUMMER-2026 (finished)
INSERT INTO semesters (semester_id, term, year, start_date, end_date, withdrawal_deadline) VALUES
    ('SPRING-2026', 'SPRING', 2026, '2026-02-01', '2026-05-31', '2026-04-15'),
    ('SUMMER-2026', 'SUMMER', 2026, '2026-06-15', '2026-08-10', '2026-07-20');

INSERT INTO registration_periods (semester_id, start_date, end_date) VALUES
    ('SPRING-2026', '2026-01-10', '2026-01-30'),
    ('SUMMER-2026', '2026-05-25', '2026-06-10');

-- FALL-2026 is the active registration semester; dates relative to today so the demo always works
INSERT INTO semesters (semester_id, term, year, start_date, end_date, withdrawal_deadline) VALUES
    ('FALL-2026', 'FALL', YEAR(CURDATE()),
     DATE_SUB(CURDATE(), INTERVAL 14 DAY),
     DATE_ADD(CURDATE(), INTERVAL 100 DAY),
     DATE_ADD(CURDATE(), INTERVAL 60 DAY));

INSERT INTO registration_periods (semester_id, start_date, end_date) VALUES
    ('FALL-2026', DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 20 DAY));

-- Sections -------------------------------------------------------------------
-- Past sections (SPRING-2026)
INSERT INTO course_sections (section_id, course_code, instructor_id, semester_id, section_no, capacity) VALUES
    ('SEC-CS101-S26-01', 'CS101',   'inst-laila', 'SPRING-2026', '01', 30),
    ('SEC-MATH-S26-01',  'MATH101', 'inst-omar',  'SPRING-2026', '01', 30),
    ('SEC-CS102-S26-01', 'CS102',   'inst-laila', 'SPRING-2026', '01', 30),
    ('SEC-HUM101-S26-01','HUM101',  'inst-yousef','SPRING-2026', '01', 30);

INSERT INTO meeting_times (section_id, day, start_time, end_time, room) VALUES
    ('SEC-CS101-S26-01', 'MONDAY',    '09:00', '10:30', 'A-101'),
    ('SEC-CS101-S26-01', 'WEDNESDAY', '09:00', '10:30', 'A-101'),
    ('SEC-MATH-S26-01',  'TUESDAY',   '09:00', '10:30', 'B-201'),
    ('SEC-MATH-S26-01',  'THURSDAY',  '09:00', '10:30', 'B-201'),
    ('SEC-CS102-S26-01', 'TUESDAY',   '11:00', '12:30', 'A-201'),
    ('SEC-CS102-S26-01', 'THURSDAY',  '11:00', '12:30', 'A-201'),
    ('SEC-HUM101-S26-01','FRIDAY',    '10:00', '12:00', 'H-100');

-- Past sections (SUMMER-2026)
INSERT INTO course_sections (section_id, course_code, instructor_id, semester_id, section_no, capacity) VALUES
    ('SEC-CS201-U26-01', 'CS201', 'inst-yousef', 'SUMMER-2026', '01', 30);

INSERT INTO meeting_times (section_id, day, start_time, end_time, room) VALUES
    ('SEC-CS201-U26-01', 'SUNDAY', '17:00', '19:30', 'A-301');

-- Current sections (FALL-2026)
INSERT INTO course_sections (section_id, course_code, instructor_id, semester_id, section_no, capacity) VALUES
    ('SEC-CS101-F26-01',  'CS101',   'inst-omar',   'FALL-2026', '01', 3),   -- small: capacity demo
    ('SEC-CS101-F26-02',  'CS101',   'inst-omar',   'FALL-2026', '02', 30),
    ('SEC-MATH-F26-01',   'MATH101', 'inst-laila',  'FALL-2026', '01', 30),
    ('SEC-CS102-F26-01',  'CS102',   'inst-laila',  'FALL-2026', '01', 30),
    ('SEC-CS201-F26-01',  'CS201',   'inst-yousef', 'FALL-2026', '01', 30),
    ('SEC-HUM101-F26-01', 'HUM101',  'inst-yousef', 'FALL-2026', '01', 30);

INSERT INTO meeting_times (section_id, day, start_time, end_time, room) VALUES
    ('SEC-CS101-F26-01', 'MONDAY',    '09:00', '10:30', 'A-101'),
    ('SEC-CS101-F26-01', 'WEDNESDAY', '09:00', '10:30', 'A-101'),
    ('SEC-CS101-F26-02', 'TUESDAY',   '09:00', '10:30', 'A-102'),
    ('SEC-CS101-F26-02', 'THURSDAY',  '09:00', '10:30', 'A-102'),
    ('SEC-MATH-F26-01',  'MONDAY',    '09:00', '10:30', 'B-201'),  -- conflicts with CS101-F26-01
    ('SEC-MATH-F26-01',  'WEDNESDAY', '09:00', '10:30', 'B-201'),
    ('SEC-CS102-F26-01', 'TUESDAY',   '11:00', '12:30', 'A-201'),
    ('SEC-CS102-F26-01', 'THURSDAY',  '11:00', '12:30', 'A-201'),
    ('SEC-CS201-F26-01', 'MONDAY',    '13:00', '14:30', 'A-301'),
    ('SEC-CS201-F26-01', 'WEDNESDAY', '13:00', '14:30', 'A-301'),
    ('SEC-HUM101-F26-01','FRIDAY',    '10:00', '12:00', 'H-100');

-- Enrollments ----------------------------------------------------------------
-- Ahmed SPRING-2026: passed CS101 (A) and MATH101 (C+)
INSERT INTO enrollments (student_id, section_id, status, score, enrolled_on, withdrawn_on) VALUES
    ('stu-ahmed', 'SEC-CS101-S26-01', 'COMPLETED', 92, '2026-01-20', NULL),
    ('stu-ahmed', 'SEC-MATH-S26-01',  'COMPLETED', 78, '2026-01-20', NULL),
    ('stu-ahmed', 'SEC-CS102-F26-01', 'ENROLLED',  NULL, CURDATE(), NULL),
    ('stu-ahmed', 'SEC-CS201-F26-01', 'ENROLLED',  NULL, CURDATE(), NULL);

-- Sara SPRING-2026: failed CS101 -> PROBATION
INSERT INTO enrollments (student_id, section_id, status, score, enrolled_on, withdrawn_on) VALUES
    ('stu-sara', 'SEC-CS101-S26-01', 'COMPLETED', 55, '2026-01-20', NULL);

-- Khalid (diploma) SPRING+SUMMER: finished all required + elective -> eligible to graduate
INSERT INTO enrollments (student_id, section_id, status, score, enrolled_on, withdrawn_on) VALUES
    ('stu-khalid', 'SEC-CS101-S26-01',  'COMPLETED', 88, '2026-01-20', NULL),
    ('stu-khalid', 'SEC-MATH-S26-01',   'COMPLETED', 91, '2026-01-20', NULL),
    ('stu-khalid', 'SEC-CS102-S26-01',  'COMPLETED', 74, '2026-01-20', NULL),
    ('stu-khalid', 'SEC-HUM101-S26-01', 'COMPLETED', 82, '2026-01-20', NULL),
    ('stu-khalid', 'SEC-CS201-U26-01',  'COMPLETED', 77, '2026-06-01', NULL);

-- Nour: no history yet; already enrolled in the nearly-full CS101-01 (capacity 3)
INSERT INTO enrollments (student_id, section_id, status, score, enrolled_on, withdrawn_on) VALUES
    ('stu-nour', 'SEC-CS101-F26-01', 'ENROLLED', NULL, CURDATE(), NULL),
    ('stu-sara', 'SEC-CS101-F26-01', 'ENROLLED', NULL, CURDATE(), NULL);

-- A welcome notification
INSERT INTO notifications (recipient_id, message, sent_at) VALUES
    ('stu-ahmed', 'Welcome to the Fall 2026 registration system. Check your inbox for schedule updates.', NOW());
