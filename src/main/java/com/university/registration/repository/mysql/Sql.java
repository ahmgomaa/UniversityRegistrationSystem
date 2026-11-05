package com.university.registration.repository.mysql;

import java.sql.SQLException;

/** Small helpers shared by the JDBC repositories. */
final class Sql {

    private Sql() {
    }

    static RuntimeException wrap(SQLException e) {
        return new IllegalStateException("Database error: " + e.getMessage(), e);
    }
}
