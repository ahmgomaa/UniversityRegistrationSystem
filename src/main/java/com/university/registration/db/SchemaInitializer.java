package com.university.registration.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates the schema (schema.sql) and optionally loads the demo data
 * (seed.sql). Statements are split on semicolons; the scripts contain no
 * stored programs, so plain splitting is safe.
 */
public final class SchemaInitializer {

    private SchemaInitializer() {
    }

    /** True when the university_registration database already has its tables. */
    public static boolean schemaExists(DatabaseConnection db) {
        try (Connection connection = db.server();
             Statement statement = connection.createStatement()) {
            statement.execute("USE university_registration");
            statement.execute("SELECT 1 FROM students LIMIT 1");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /** Drops and recreates the schema, optionally seeding demo data. */
    public static void initialize(DatabaseConnection db, boolean withSeed) throws SQLException {
        try (Connection connection = db.server();
             Statement statement = connection.createStatement()) {
            for (String sql : statements("/schema.sql")) {
                statement.execute(sql);
            }
            if (withSeed) {
                for (String sql : statements("/seed.sql")) {
                    statement.execute(sql);
                }
            }
        }
    }

    private static List<String> statements(String resource) {
        String script = readResource(resource);
        List<String> result = new ArrayList<>();
        for (String part : script.split(";")) {
            if (hasExecutableContent(part)) {
                result.add(part.trim());
            }
        }
        return result;
    }

    /** A part is executable when it contains a line that is not blank or a comment. */
    private static boolean hasExecutableContent(String part) {
        return part.lines()
                .map(String::trim)
                .anyMatch(line -> !line.isEmpty() && !line.startsWith("--"));
    }

    private static String readResource(String resource) {
        try (InputStream in = SchemaInitializer.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Missing resource " + resource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + resource, e);
        }
    }

    /** Lists table names (helper used by the console rescan/reseed flow). */
    public static List<String> tables(DatabaseConnection db) throws SQLException {
        List<String> names = new ArrayList<>();
        try (Connection connection = db.get();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SHOW TABLES")) {
            while (rs.next()) {
                names.add(rs.getString(1));
            }
        }
        return names;
    }
}
