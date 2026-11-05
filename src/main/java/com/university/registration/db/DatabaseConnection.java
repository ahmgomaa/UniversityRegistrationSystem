package com.university.registration.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Provides JDBC connections. Settings come from db.properties on the
 * classpath and can be overridden with the DB_URL, DB_USER and DB_PASSWORD
 * environment variables.
 */
public final class DatabaseConnection {

    private final String url;
    private final String user;
    private final String password;

    private DatabaseConnection(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static DatabaseConnection fromProperties() {
        Properties props = new Properties();
        try (InputStream in = DatabaseConnection.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read db.properties", e);
        }
        String url = System.getenv().getOrDefault("DB_URL", props.getProperty("db.url", "jdbc:mysql://localhost:3306/university_registration"));
        String user = System.getenv().getOrDefault("DB_USER", props.getProperty("db.user", "root"));
        String password = System.getenv().getOrDefault("DB_PASSWORD", props.getProperty("db.password", ""));
        return new DatabaseConnection(url, user, password);
    }

    /** URL to the server without any database selected (used by schema setup). */
    public String serverUrl() {
        int dbPart = url.lastIndexOf('/');
        String head = dbPart >= 0 ? url.substring(0, dbPart + 1) : url;
        return head + "?allowMultiQueries=false&useSSL=false&serverTimezone=UTC";
    }

    public String url() {
        return url;
    }

    public Connection get() throws SQLException {
        String separator = url.contains("?") ? "&" : "?";
        return DriverManager.getConnection(url + separator + "useSSL=false&serverTimezone=UTC", user, password);
    }

    public Connection server() throws SQLException {
        return DriverManager.getConnection(serverUrl(), user, password);
    }

    /** Whether the database answers connections right now. */
    public boolean canConnect() {
        try (Connection ignored = server()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
