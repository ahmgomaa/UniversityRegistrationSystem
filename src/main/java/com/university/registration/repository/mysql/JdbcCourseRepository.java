package com.university.registration.repository.mysql;

import com.university.registration.db.DatabaseConnection;
import com.university.registration.model.Course;
import com.university.registration.model.Department;
import com.university.registration.port.CourseRepository;
import com.university.registration.port.DepartmentRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Loads courses including their (recursive) prerequisite chains. Hydration is
 * a memoized depth-first walk over course_prerequisites; cycles in the data
 * are rejected.
 */
public class JdbcCourseRepository implements CourseRepository {

    private final DatabaseConnection db;
    private final DepartmentRepository departments;

    public JdbcCourseRepository(DatabaseConnection db, DepartmentRepository departments) {
        this.db = db;
        this.departments = departments;
    }

    @Override
    public List<Course> findAll() {
        try (Connection c = db.get();
             var st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT course_code FROM courses ORDER BY course_code")) {
            List<String> codes = new java.util.ArrayList<>();
            while (rs.next()) {
                codes.add(rs.getString("course_code"));
            }
            Map<String, Course> memo = new HashMap<>();
            return codes.stream()
                    .map(code -> hydrate(c, code, memo, new HashSet<>()))
                    .toList();
        } catch (SQLException e) {
            throw Sql.wrap(e);
        }
    }

    @Override
    public Optional<Course> findById(String courseCode) {
        try (Connection c = db.get()) {
            return Optional.ofNullable(hydrate(c, courseCode, new HashMap<>(), new HashSet<>()));
        } catch (SQLException e) {
            throw Sql.wrap(e);
        }
    }

    private Course hydrate(Connection c, String code, Map<String, Course> memo, Set<String> visiting) {
        Course cached = memo.get(code);
        if (cached != null) {
            return cached;
        }
        if (!visiting.add(code)) {
            throw new IllegalStateException("Cyclic prerequisite chain involving " + code);
        }
        try {
            Course course = loadRow(c, code, memo, visiting);
            memo.put(code, course);
            visiting.remove(code);
            return course;
        } catch (SQLException e) {
            throw Sql.wrap(e);
        }
    }

    private Course loadRow(Connection c, String code, Map<String, Course> memo, Set<String> visiting)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT * FROM courses WHERE course_code = ?")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Department dept = departments.findById(rs.getString("dept_code"))
                        .orElseThrow(() -> new IllegalStateException("Course " + code + " has no department"));
                Set<Course> prerequisites = new HashSet<>();
                try (PreparedStatement prereq = c.prepareStatement(
                        "SELECT prereq_code FROM course_prerequisites WHERE course_code = ?")) {
                    prereq.setString(1, code);
                    try (ResultSet prs = prereq.executeQuery()) {
                        while (prs.next()) {
                            Course p = hydrate(c, prs.getString(1), memo, visiting);
                            if (p != null) {
                                prerequisites.add(p);
                            }
                        }
                    }
                }
                return new Course(code, rs.getString("course_name"), rs.getInt("credit_hours"),
                        prerequisites, dept);
            }
        }
    }
}
