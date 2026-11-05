package com.university.registration.repository.mysql;

import com.university.registration.db.DatabaseConnection;
import com.university.registration.model.AcademicStaff;
import com.university.registration.model.Department;
import com.university.registration.model.Instructor;
import com.university.registration.model.User;
import com.university.registration.model.enums.StaffRole;
import com.university.registration.port.DepartmentRepository;
import com.university.registration.port.StaffRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class JdbcStaffRepository implements StaffRepository {

    private final DatabaseConnection db;
    private final DepartmentRepository departments;

    public JdbcStaffRepository(DatabaseConnection db, DepartmentRepository departments) {
        this.db = db;
        this.departments = departments;
    }

    @Override
    public List<Instructor> findAllInstructors() {
        try (Connection c = db.get();
             var st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT * FROM users WHERE user_type = 'INSTRUCTOR' ORDER BY name")) {
            List<Instructor> result = new ArrayList<>();
            while (rs.next()) {
                result.add(instructor(c, rs));
            }
            return result;
        } catch (SQLException e) {
            throw Sql.wrap(e);
        }
    }

    @Override
    public List<AcademicStaff> findAllStaff() {
        try (Connection c = db.get();
             var st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT * FROM users WHERE user_type = 'ACADEMIC_STAFF' ORDER BY name")) {
            List<AcademicStaff> result = new ArrayList<>();
            while (rs.next()) {
                result.add(staff(c, rs));
            }
            return result;
        } catch (SQLException e) {
            throw Sql.wrap(e);
        }
    }

    @Override
    public Optional<Instructor> findInstructorById(String userId) {
        try (Connection c = db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM users WHERE user_id = ? AND user_type = 'INSTRUCTOR'")) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(instructor(c, rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw Sql.wrap(e);
        }
    }

    @Override
    public Optional<AcademicStaff> findStaffById(String userId) {
        try (Connection c = db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM users WHERE user_id = ? AND user_type = 'ACADEMIC_STAFF'")) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(staff(c, rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw Sql.wrap(e);
        }
    }

    private Instructor instructor(Connection c, ResultSet rs) throws SQLException {
        return new Instructor(
                rs.getString("user_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("phone_number"),
                departmentOf(rs));
    }

    private AcademicStaff staff(Connection c, ResultSet rs) throws SQLException {
        Set<StaffRole> roles = new HashSet<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT role FROM academic_staff_roles WHERE user_id = ?")) {
            ps.setString(1, rs.getString("user_id"));
            try (ResultSet rrs = ps.executeQuery()) {
                while (rrs.next()) {
                    roles.add(StaffRole.valueOf(rrs.getString("role")));
                }
            }
        }
        return new AcademicStaff(
                rs.getString("user_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("phone_number"),
                departmentOf(rs),
                roles);
    }

    private Department departmentOf(ResultSet rs) throws SQLException {
        String code = rs.getString("dept_code");
        return code == null ? null : departments.findById(code).orElseThrow();
    }
}
