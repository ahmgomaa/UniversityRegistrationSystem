package db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class SchemaInitializer {

    public static void ensureSchema(Connection connection) throws SQLException, IOException {
        if (schemaExists(connection)) {
            return;
        }
        System.out.println("Tables not found - creating the schema (schema.sql)...");
        executeScript(connection, "/schema.sql");
        executeScript(connection, "/demo_data.sql");
        System.out.println("Schema and demo data created.");
    }

    private static boolean schemaExists(Connection connection) throws SQLException {
        ResultSet tables = connection.getMetaData().getTables(null, null, "students", new String[]{"TABLE"});
        boolean exists = tables.next();
        tables.close();
        return exists;
    }

    private static void executeScript(Connection connection, String resourcePath)
            throws SQLException, IOException {
        ArrayList<String> statements = readStatements(resourcePath);
        try (Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.executeUpdate(sql);
            }
        }
    }

    private static ArrayList<String> readStatements(String resourcePath) throws IOException {
        ArrayList<String> statements = new ArrayList<String>();
        InputStream in = SchemaInitializer.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            int c = reader.read();
            while (c != -1) {
                text.append((char) c);
                c = reader.read();
            }
        }
        String[] parts = text.toString().split(";");
        for (String part : parts) {
            String sql = part.trim();
            if (!sql.isEmpty()) {
                statements.add(sql);
            }
        }
        return statements;
    }
}
