package com.university.registration.repository.mysql;

import com.university.registration.db.DatabaseConnection;
import com.university.registration.model.AcademicRecord;
import com.university.registration.model.AcademicStaff;
import com.university.registration.model.CourseSection;
import com.university.registration.model.Enrollment;
import com.university.registration.model.Grade;
import com.university.registration.model.Program;
import com.university.registration.model.Student;
import com.university.registration.model.enums.AcademicStatus;
import com.university.registration.model.enums.EnrollmentStatus;
import com.university.registration.port.ProgramRepository;
import com.university.registration.port.SectionRepository;
import com.university.registration.port.StaffRepository;
import com.university.registration.port.StudentRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Loads students as full aggregates: program, advisor and the complete
 * enrollment history with sections, meetings, instructors and semesters.
 */
public class JdbcStudentRepository implements StudentRepository {

    private final DatabaseConnection db;
    private final ProgramRepository programs;
    private final StaffRepository staff;
    private final SectionRepository sections;

    public JdbcStudentRepository(DatabaseConnection db, ProgramRepository programs,
                                 StaffRepository staff, SectionRepository sections) {
        this.db = db;
        this.programs = programs;
        this.staff = staff;
        this.sections = sections;
    }

    @Override
    public List<Student> findAll() {
        try (Connection c = db.get();
             var st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT user_id FROM students ORDER BY student_number")) {
            List<String> ids = new ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getString("user_id"));
            }
            List<Student> result = new ArrayList<>();
            for (String id : ids) {
                findById(id).ifPresent(result::add);
            }
            return result;
        } catch (SQLException e) {
            throw Sql.wrap(e);
        }
    }

    @Override
    public Optional<Student> findById(String userId) {
        try (Connection c = db.get();
             PreparedStatement ps = c.prepareStatement(
                     """
                     SELECT u.user_id, u.name, u.email, u.password_hash, u.phone_number,
                            s.student_number, s.admission_year, s.academic_status, s.program_id, s.advisor_id
                     FROM users u JOIN students s ON s.user_id = u.user_id
                     WHERE u.user_id = ? AND u.user_type = 'STUDENT'
                     """)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Program program = programs.findById(rs.getInt("program_id"))
                        .orElseThrow(() -> new IllegalStateException("Student has no program"));
                AcademicStaff advisor = null;
                String advisorId = rs.getString("advisor_id");
                if (advisorId != null) {
                    advisor = staff.findStaffById(advisorId).orElse(null);
                }
                Student student = new Student(
                        rs.getString("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("phone_number"),
                        rs.getString("student_number"),
                        rs.getInt("admission_year"),
                        AcademicStatus.valueOf(rs.getString("academic_status")),
                        program,
                        advisor);
                loadEnrollments(c, student);
                return Optional.of(student);
            }
        } catch (SQLException e) {
            throw Sql.wrap(e);
        }
    }

    @Override
    public void save(Student student) {
        try (Connection c = db.get();
             PreparedStatement userPs = c.prepareStatement(
                     """
                     INSERT INTO users (user_id, user_type, name, email, password_hash, phone_number)
                     VALUES (?, 'STUDENT', ?, ?, ?, ?)
                     ON DUPLICATE KEY UPDATE name = VALUES(name), email = VALUES(email),
                             password_hash = VALUES(password_hash), phone_number = VALUES(phone_number)
                     """);
             PreparedStatement studentPs = c.prepareStatement(
                     """
                     INSERT INTO students (user_id, student_number, admission_year, academic_status, program_id, advisor_id)
                     VALUES (?, ?, ?, ?, ?, ?)
                     ON DUPLICATE KEY UPDATE student_number = VALUES(student_number),
                             admission_year = VALUES(admission_year), academic_status = VALUES(academic_status),
                             program_id = VALUES(program_id), advisor_id = VALUES(advisor_id)
                     """)) {
            userPs.setString(1, student.getUserId());
            userPs.setString(2, student.getName());
            userPs.setString(3, student.getEmail());
            userPs.setString(4, User_hash(student));
            userPs.setString(5, student.getPhoneNumber());
            userPs.executeUpdate();

            studentPs.setString(1, student.getUserId());
            studentPs.setString(2, student.getStudentNumber());
            studentPs.setInt(3, student.getAdmissionYear());
            studentPs.setString(4, student.getAcademicStatus().name());
            studentPs.setInt(5, student.getProgram().getProgramId());
            studentPs.setString(6, student.getAdvisor() == null ? null : student.getAdvisor().getUserId());
            studentPs.executeUpdate();
        } catch (SQLException e) {
            throw Sql.wrap(e);
        }
    }

    private String User_hash(Student student) {
        // password hash is stored in the users row; User exposes only verify/change,
        // so the existing hash is preserved by reading it back if the row exists.
        return currentPasswordHash(student.getUserId());
    }

    private void loadEnrollments(Connection c, Student student) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                """
                SELECT section_id, status, score, enrolled_on, withdrawn_on
                FROM enrollments WHERE student_id = ? ORDER BY enrollment_id
                """)) {
            ps.setString(1, student.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                AcademicRecord record = student.getAcademicRecord();
                while (rs.next()) {
                    CourseSection section = sections.findById(rs.getString("section_id"))
                            .orElseThrow(() -> new IllegalStateException("Enrollment has no section"));
                    Enrollment enrollment = new Enrollment(section, rs.getObject("enrolled_on", LocalDate.class));
                    Double score = rs.getObject("score") == null ? null : rs.getDouble("score");
                    enrollment.restoreState(
                            EnrollmentStatus.valueOf(rs.getString("status")),
                            score == null ? null : new Grade(score),
                            rs.getObject("withdrawn_on", LocalDate.class));
                    record.addEnrollment(enrollment);
                }
            }
        }
    }
}
