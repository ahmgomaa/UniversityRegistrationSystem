package com.university.registration.repository.mysql;

import com.university.registration.db.DatabaseConnection;
import com.university.registration.model.Department;
import com.university.registration.port.DepartmentRepository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcDepartmentRepository implements DepartmentRepository {

    private final DatabaseConnection db;

    public JdbcDepartmentRepository(DatabaseConnection db) {
        this.db = db;
    }

    @Override
    public List<Department> findAll() {
        try (Connection c = db.get();
             var st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM departments ORDER BY dept_code")) {
            List<Department> result = new ArrayList<>();
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw Sql.wrap(e);
        }
    }

    @Override
    public Optional<Department> findById(String deptCode) {
        try (Connection c = db.get();
             var ps = c.prepareStatement("SELECT * FROM departments WHERE dept_code = ?")) {
            ps.setString(1, deptCode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw Sql.wrap(e);
        }
    }

    private Department map(ResultSet rs) throws SQLException {
        return new Department(rs.getString("dept_code"), rs.getString("dept_name"));
    }
}
