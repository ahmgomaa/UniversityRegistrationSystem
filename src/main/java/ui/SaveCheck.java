package ui;

import db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Verifies, with a fresh read from the database, that a change the UI
 * just made really reached the database.
 *
 * DataStore.save() never reports a failure — it catches every
 * SQLException and only prints it — so a silent save tells the UI
 * nothing. These checks ask the database directly instead, using the
 * same connection settings the app itself uses
 * (DatabaseConnection.fromProperties). Every method returns false,
 * never throws, when the row is missing, different, or unreadable,
 * so the UI can show "not saved" instead of pretending it worked.
 */
public final class SaveCheck {

    private SaveCheck() {
    }

    private static Connection open() throws SQLException {
        return DatabaseConnection.fromProperties().getConnection();
    }

    /** True when the enrollment row exists with the expected status. */
    public static boolean enrollmentSaved(String studentId, String sectionId,
                                          String expectedStatus) {
        String sql = "SELECT status FROM enrollments WHERE user_id = ? AND section_id = ?";
        try (Connection connection = open();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && expectedStatus.equals(rs.getString(1));
            }
        } catch (SQLException e) {
            return false;
        }
    }

    /** True when the enrollment is COMPLETED with the recorded score. */
    public static boolean gradeSaved(String studentId, String sectionId, int score) {
        String sql = "SELECT status, score FROM enrollments WHERE user_id = ? AND section_id = ?";
        try (Connection connection = open();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                return "COMPLETED".equals(rs.getString(1)) && score == rs.getInt(2);
            }
        } catch (SQLException e) {
            return false;
        }
    }

    /** True when the student's status (and advisor) match in the database. */
    public static boolean studentSaved(String studentId, String expectedStatus,
                                       String expectedAdvisorId) {
        String sql = "SELECT academic_status, advisor_id FROM students WHERE user_id = ?";
        try (Connection connection = open();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                if (!expectedStatus.equals(rs.getString(1))) {
                    return false;
                }
                String advisorId = rs.getString(2);
                return expectedAdvisorId == null ? advisorId == null
                        : expectedAdvisorId.equals(advisorId);
            }
        } catch (SQLException e) {
            return false;
        }
    }

    /** True when the stored password hash matches the new one. */
    public static boolean passwordSaved(String userId, String expectedHash) {
        String sql = "SELECT password_hash FROM users WHERE user_id = ?";
        try (Connection connection = open();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && expectedHash.equals(rs.getString(1));
            }
        } catch (SQLException e) {
            return false;
        }
    }
}
