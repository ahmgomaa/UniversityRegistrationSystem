package com.university.registration.repository.mysql;

import com.university.registration.db.DatabaseConnection;
import com.university.registration.model.RegistrationPeriod;
import com.university.registration.model.Semester;
import com.university.registration.model.enums.Term;
import com.university.registration.port.SemesterRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcSemesterRepository implements SemesterRepository {

    private final DatabaseConnection db;

    public JdbcSemesterRepository(DatabaseConnection db) {
        this.db = db;
    }

    @Override
    public List<Semester> findAll() {
        try (Connection c = db.get();
             var st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT * FROM semesters ORDER BY year, FIELD(term, 'SPRING', 'SUMMER', 'FALL')")) {
            List<Semester> result = new ArrayList<>();
            while (rs.next()) {
                result.add(map(c, rs));
            }
            return result;
        } catch (SQLException e) {
            throw Sql.wrap(e);
        }
    }

    @Override
    public Optional<Semester> findById(String semesterId) {
        try (Connection c = db.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM semesters WHERE semester_id = ?")) {
            ps.setString(1, semesterId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(c, rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw Sql.wrap(e);
        }
    }

    private Semester map(Connection c, ResultSet rs) throws SQLException {
        RegistrationPeriod period = null;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT * FROM registration_periods WHERE semester_id = ?")) {
            ps.setString(1, rs.getString("semester_id"));
            try (ResultSet prs = ps.executeQuery()) {
                if (prs.next()) {
                    period = new RegistrationPeriod(
                            prs.getObject("start_date", LocalDate.class),
                            prs.getObject("end_date", LocalDate.class));
                }
            }
        }
        if (period == null) {
            throw new IllegalStateException("Semester " + rs.getString("semester_id")
                    + " has no registration period");
        }
        return new Semester(
                rs.getString("semester_id"),
                Term.valueOf(rs.getString("term")),
                rs.getInt("year"),
                rs.getObject("start_date", LocalDate.class),
                rs.getObject("end_date", LocalDate.class),
                rs.getObject("withdrawal_deadline", LocalDate.class),
                period);
    }
}
