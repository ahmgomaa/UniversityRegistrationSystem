package db;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConnection {

    private final String url;
    private final String user;
    private final String password;

    public DatabaseConnection(String url, String user, String password) {
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
        } catch (Exception e) {
            System.out.println("[WARN] Could not read db.properties: " + e.getMessage());
        }
        String url = System.getenv("DB_URL");
        if (url == null) {
            url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/university_registration");
        }
        String user = System.getenv("DB_USER");
        if (user == null) {
            user = props.getProperty("db.user", "root");
        }
        String password = System.getenv("DB_PASSWORD");
        if (password == null) {
            password = props.getProperty("db.password", "");
        }
        return new DatabaseConnection(url, user, password);
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void createDatabase() throws SQLException {
        String databaseName = extractDatabaseName(url);
        if (databaseName == null) {
            return;
        }
        String serverUrl = url.substring(0, url.lastIndexOf("/" + databaseName));
        try (Connection connection = DriverManager.getConnection(serverUrl, user, password);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);
        }
    }

    private static String extractDatabaseName(String url) {
        String withoutParams = url;
        int paramIndex = url.indexOf("?");
        if (paramIndex >= 0) {
            withoutParams = url.substring(0, paramIndex);
        }
        int slashIndex = withoutParams.lastIndexOf("/");
        if (slashIndex < 0 || slashIndex == withoutParams.length() - 1) {
            return null;
        }
        return withoutParams.substring(slashIndex + 1);
    }
}
