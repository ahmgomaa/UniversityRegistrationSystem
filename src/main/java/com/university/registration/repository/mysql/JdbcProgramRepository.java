package com.university.registration.repository.mysql;

import com.university.registration.db.DatabaseConnection;
import com.university.registration.model.Course;
import com.university.registration.model.Department;
import com.university.registration.model.Program;
import com.university.registration.port.CourseRepository;
import com.university.registration.port.DepartmentRepository;
import com.university.registration.port.ProgramRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class JdbcProgramRepository implements ProgramRepository {

    private final DatabaseConnection db;
    private final DepartmentRepository departments;
    private final CourseRepository courses;

    public JdbcProgramRepository(DatabaseConnection db, DepartmentRepository departments,
                                 CourseRepository courses) {
        this.db = db;
        this.departments = departments;
        this.courses = courses;
    }

    @Override
    public List<Program> findAll() {
        try (Connection c = db.get();
             var st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM programs ORDER BY program_id")) {
            List<Program> result = new ArrayList<>();
            while (rs.next()) {
                result.add(map(c, rs));
            }
            return result;
        } catch (SQLException e) {
            throw Sql.wrap(e);
        }
    }

    @Override
    public Optional<Program> findById(int programId) {
        try (Connection c = db.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM programs WHERE program_id = ?")) {
            ps.setInt(1, programId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(c, rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw Sql.wrap(e);
        }
    }

    private Program map(Connection c, ResultSet rs) throws SQLException {
        Department department = departments.findById(rs.getString("dept_code"))
                .orElseThrow(() -> new IllegalStateException("Program has no department"));
        int programId = rs.getInt("program_id");
        return new Program(
                programId,
                rs.getString("name"),
                department,
                rs.getInt("total_credits_required"),
                rs.getDouble("minimum_cgpa"),
                coursesOf(c, programId, "REQUIRED"),
                coursesOf(c, programId, "ELECTIVE"),
                rs.getInt("elective_credits"),
                rs.getInt("max_credits_per_semester"),
                rs.getInt("probation_max_credits"),
                rs.getDouble("probation_cgpa"),
                rs.getInt("number_of_levels"));
    }

    private Set<Course> coursesOf(Connection c, int programId, String kind) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT course_code FROM program_courses WHERE program_id = ? AND kind = ?")) {
            ps.setInt(1, programId);
            ps.setString(2, kind);
            try (ResultSet rs = ps.executeQuery()) {
                Set<Course> result = new HashSet<>();
                while (rs.next()) {
                    courses.findById(rs.getString(1)).ifPresent(result::add);
                }
                return result;
            }
        }
    }
}
