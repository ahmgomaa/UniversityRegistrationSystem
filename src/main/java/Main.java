import data.DataStore;
import db.DatabaseConnection;
import db.SchemaInitializer;
import service.GraduationService;
import service.GradingService;
import service.InAppNotificationService;
import service.RegistrationService;
import service.ReportService;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        DatabaseConnection db = DatabaseConnection.fromProperties();
        Connection connection;
        try {
            connection = db.getConnection();
        } catch (SQLException e) {
            connection = openWithDatabaseCreation(db);
            if (connection == null) {
                return;
            }
        }

        try {
            SchemaInitializer.ensureSchema(connection);
            start(connection);
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private static Connection openWithDatabaseCreation(DatabaseConnection db) {
        try {
            db.createDatabase();
            return db.getConnection();
        } catch (SQLException e) {
            System.out.println("[ERROR] Cannot connect to MySQL: " + e.getMessage());
            System.out.println("Check the settings in src/main/resources/db.properties");
            return null;
        }
    }

    private static void start(Connection connection) throws Exception {
        DataStore store = new DataStore(connection);
        store.load();

        InAppNotificationService notifications =
                new InAppNotificationService(store.getNotifications());
        RegistrationService registration =
                new RegistrationService(notifications, store);
        GradingService grading =
                new GradingService(notifications, store);
        GraduationService graduation = new GraduationService();
        ReportService reports = new ReportService();

        ConsoleUI ui = new ConsoleUI(store, registration, grading, graduation, reports, notifications);
        ui.run();
        connection.close();
    }
}
