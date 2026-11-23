INSERT INTO departments (dept_code, dept_name) VALUES
('CS', 'Computer Science'),
('MA', 'Mathematics');

INSERT INTO users (user_id, name, email, password_hash, phone, user_type) VALUES
('CS-1001', 'Ahmed Samir', 'ASU@CS-1001', '8641', '0110000001', 'STUDENT'),
('CS-1002', 'Sara Magdy', 'ASU@CS-1002', '8641', '0110000002', 'STUDENT'),
('CS-1003', 'Nour El-Sayed', 'ASU@CS-1003', '8641', '0110000003', 'STUDENT'),
('CS-1004', 'Khalid Fathi', 'ASU@CS-1004', '8641', '0110000004', 'STUDENT'),
('IN-101', 'Omar Khaled', 'ASU@IN-101', '8641', '0100000001', 'INSTRUCTOR'),
('IN-102', 'Laila Hassan', 'ASU@IN-102', '8641', '0100000002', 'INSTRUCTOR'),
('IN-103', 'Yousef Adel', 'ASU@IN-103', '8641', '0100000003', 'INSTRUCTOR'),
('AS-11', 'Mona Sabet', 'ASU@AS-11', '8641', '0100000004', 'STAFF');

INSERT INTO staff_members (user_id, dept_code, roles) VALUES
('IN-101', 'CS', ''),
('IN-102', 'CS', ''),
('IN-103', 'MA', ''),
('AS-11', 'CS', 'ADVISOR,REGISTRAR');

INSERT INTO programs (program_id, name, dept_code, total_credits, min_cgpa, elective_credits,
                      max_credits_per_semester, probation_max_credits, probation_cgpa, number_of_levels) VALUES
(1, 'BSc Computer Science', 'CS', 18, 2.0, 3, 9, 6, 2.0, 4),
(2, 'Data Science Diploma', 'MA', 6, 2.0, 3, 9, 6, 2.0, 1);

INSERT INTO courses (course_code, course_name, credit_hours, dept_code) VALUES
('CS101', 'Introduction to Programming', 3, 'CS'),
('CS102', 'Object-Oriented Programming', 3, 'CS'),
('CS201', 'Data Structures', 3, 'CS'),
('CS301', 'Software Engineering', 3, 'CS'),
('CS210', 'Web Development', 3, 'CS'),
('MATH101', 'Calculus I', 3, 'MA'),
('MATH201', 'Linear Algebra', 3, 'MA');

INSERT INTO course_prerequisites (course_code, prerequisite_code) VALUES
('CS102', 'CS101'),
('CS201', 'CS102'),
('CS301', 'CS201'),
('MATH201', 'MATH101');

INSERT INTO program_courses (program_id, course_code, course_kind) VALUES
(1, 'CS101', 'REQUIRED'),
(1, 'CS102', 'REQUIRED'),
(1, 'CS201', 'REQUIRED'),
(1, 'CS301', 'REQUIRED'),
(1, 'MATH101', 'REQUIRED'),
(1, 'CS210', 'ELECTIVE'),
(1, 'MATH201', 'ELECTIVE'),
(2, 'MATH101', 'REQUIRED'),
(2, 'CS210', 'REQUIRED'),
(2, 'MATH201', 'ELECTIVE');

INSERT INTO semesters (semester_id, term, year, start_date, end_date, withdrawal_deadline,
                       registration_start, registration_end) VALUES
('FALL-2025', 'FALL', 2025, '2025-09-14', '2026-01-08', '2025-10-30', '2025-08-20', '2025-09-13'),
('FALL-2026', 'FALL', 2026, CURDATE() - INTERVAL 7 DAY, CURDATE() + INTERVAL 60 DAY,
 CURDATE() + INTERVAL 21 DAY, CURDATE() - INTERVAL 7 DAY, CURDATE() + INTERVAL 30 DAY);

INSERT INTO course_sections (section_id, course_code, instructor_id, semester_id, section_no, capacity) VALUES
('SEC-CS101-F26-01', 'CS101', 'IN-101', 'FALL-2026', '01', 2),
('SEC-MATH101-F26-01', 'MATH101', 'IN-103', 'FALL-2026', '01', 30),
('SEC-CS201-F26-01', 'CS201', 'IN-101', 'FALL-2026', '01', 30),
('SEC-CS210-F26-01', 'CS210', 'IN-102', 'FALL-2026', '01', 30),
('SEC-CS101-F25-01', 'CS101', 'IN-101', 'FALL-2025', '01', 30),
('SEC-CS102-F25-01', 'CS102', 'IN-101', 'FALL-2025', '01', 30),
('SEC-MATH101-F25-01', 'MATH101', 'IN-103', 'FALL-2025', '01', 30),
('SEC-CS210-F25-01', 'CS210', 'IN-102', 'FALL-2025', '01', 30);

INSERT INTO meeting_times (section_id, meeting_day, start_time, end_time, room) VALUES
('SEC-CS101-F26-01', 'SUNDAY', '09:00:00', '10:40:00', 'B12'),
('SEC-MATH101-F26-01', 'SUNDAY', '09:30:00', '11:10:00', 'A3'),
('SEC-CS201-F26-01', 'MONDAY', '13:00:00', '14:40:00', 'B10'),
('SEC-CS210-F26-01', 'TUESDAY', '11:00:00', '12:40:00', 'B14'),
('SEC-CS101-F25-01', 'SUNDAY', '09:00:00', '10:40:00', 'B12'),
('SEC-CS102-F25-01', 'MONDAY', '09:00:00', '10:40:00', 'B10'),
('SEC-MATH101-F25-01', 'TUESDAY', '11:00:00', '12:40:00', 'B14'),
('SEC-CS210-F25-01', 'WEDNESDAY', '11:00:00', '12:40:00', 'A5');

INSERT INTO students (user_id, student_number, admission_year, academic_status, program_id, advisor_id) VALUES
('CS-1001', 'CS-1001', 2025, 'ACTIVE', 1, 'AS-11'),
('CS-1002', 'CS-1002', 2025, 'ACTIVE', 1, NULL),
('CS-1003', 'CS-1003', 2025, 'PROBATION', 1, NULL),
('CS-1004', 'CS-1004', 2024, 'GRADUATED', 2, NULL);

INSERT INTO enrollments (user_id, section_id, status, score, enrolled_on, withdrawn_on) VALUES
('CS-1001', 'SEC-CS101-F25-01', 'COMPLETED', 85, '2025-09-20', NULL),
('CS-1001', 'SEC-CS102-F25-01', 'COMPLETED', 78, '2025-09-20', NULL),
('CS-1002', 'SEC-MATH101-F25-01', 'COMPLETED', 52, '2025-09-20', NULL),
('CS-1003', 'SEC-CS101-F25-01', 'COMPLETED', 45, '2025-09-20', NULL),
('CS-1004', 'SEC-MATH101-F25-01', 'COMPLETED', 75, '2025-09-20', NULL),
('CS-1004', 'SEC-CS210-F25-01', 'COMPLETED', 88, '2025-09-20', NULL);

INSERT INTO notifications (recipient_id, message, sent_at) VALUES
('CS-1001', 'Welcome to the University Registration System!', NOW());
