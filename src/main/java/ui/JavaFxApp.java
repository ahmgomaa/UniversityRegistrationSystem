package ui;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import data.DataStore;
import db.DatabaseConnection;
import db.SchemaInitializer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.AcademicStaff;
import model.Instructor;
import model.Student;
import model.User;
import service.GradingService;
import service.GraduationService;
import service.InAppNotificationService;
import service.RegistrationService;
import service.ReportService;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * JavaFX entry point for the University Registration System.
 *
 * It wires exactly the same objects as Main (DatabaseConnection,
 * SchemaInitializer, DataStore and the services) and then shows the
 * graphical UI instead of the console. All rules live in the services;
 * this class and the views only display data and call the services.
 *
 * Look: Apple Human Interface Guidelines on AtlantaFX Cupertino
 * (light / dark appearance toggle in the top bar).
 */
public class JavaFxApp extends Application {

    private AppContext context;
    private Stage stage;
    private BorderPane root;
    private boolean darkAppearance;
    private User signedInUser;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
        this.stage = primaryStage;
        try {
            this.context = buildContext();
        } catch (Exception e) {
            UiKit.error("Cannot start",
                    "Could not connect to the database: " + e.getMessage()
                            + "\nCheck src/main/resources/db.properties and that MySQL is running.");
            return;
        }
        stage.setTitle("University Registration System");
        root = new BorderPane();
        Scene scene = new Scene(root, 1000, 680);
        stage.setScene(scene);
        showLogin();
        stage.show();
    }

    /** Same wiring as Main.main(). */
    private static AppContext buildContext() throws Exception {
        DatabaseConnection db = DatabaseConnection.fromProperties();
        Connection connection;
        try {
            connection = db.getConnection();
        } catch (SQLException e) {
            db.createDatabase();
            connection = db.getConnection();
        }
        SchemaInitializer.ensureSchema(connection);
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

        return new AppContext(store, notifications, registration, grading, graduation, reports);
    }

    /** Switch between the light and dark Cupertino appearance. */
    private void toggleAppearance() {
        darkAppearance = !darkAppearance;
        if (darkAppearance) {
            Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
        }
    }

    private HBox topBar(String title) {
        Label titleLabel = UiKit.largeTitle(title);
        Button appearanceButton = new Button(darkAppearance ? "Light" : "Dark");
        appearanceButton.setOnAction(e -> {
            toggleAppearance();
            appearanceButton.setText(darkAppearance ? "Light" : "Dark");
        });
        HBox bar = new HBox(12, titleLabel);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        bar.getChildren().add(appearanceButton);
        bar.setPadding(new Insets(16, 20, 8, 20));
        bar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return bar;
    }

    private void setScreen(String title, javafx.scene.Node content) {
        root.setTop(topBar(title));
        root.setCenter(content);
        root.setLeft(null);
    }

    /** Login screen. */
    public void showLogin() {
        signedInUser = null;
        LoginView view = new LoginView(context, this);
        setScreen("University Registration", view.build());
    }

    /** Visitor browse screen (no sign-in). */
    public void showBrowse() {
        BrowseView view = new BrowseView(context, this);
        setScreen("Browse Sections", view.build());
    }

    /** Open the right portal for the signed-in user. */
    public void signedIn(User user) {
        signedInUser = user;
        if (user instanceof Student) {
            StudentView view = new StudentView(context, this, (Student) user);
            setScreen("Student", view.build());
        } else if (user instanceof Instructor) {
            InstructorView view = new InstructorView(context, this, (Instructor) user);
            setScreen("Instructor", view.build());
        } else if (user instanceof AcademicStaff) {
            StaffView view = new StaffView(context, this, (AcademicStaff) user);
            setScreen("Academic Staff", view.build());
        }
    }

    public void signOut() {
        showLogin();
    }

    public User getSignedInUser() {
        return signedInUser;
    }
}
