package ui;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import data.DataStore;
import db.DatabaseConnection;
import db.SchemaInitializer;
import javafx.application.Application;
import javafx.scene.Scene;
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

public class JavaFxApp extends Application {

    private AppContext context;
    private Stage stage;
    private ShellController shell;
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
        Fxml<ShellController> shellScreen = Fxml.load("Shell");
        shell = shellScreen.getController();
        shell.init(this);
        Scene scene = new Scene(shellScreen.getRoot(), 1000, 680);
        stage.setScene(scene);
        showLogin();
        stage.show();
    }

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

    public void toggleAppearance() {
        darkAppearance = !darkAppearance;
        if (stage != null && stage.getScene() != null) {
            if (darkAppearance) {
                stage.getScene().getRoot().getStyleClass().add("dark");
            } else {
                stage.getScene().getRoot().getStyleClass().remove("dark");
            }
        }
        if (darkAppearance) {
            Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
        }
    }

    public boolean isDarkAppearance() {
        return darkAppearance;
    }

    private void setScreen(String title, javafx.scene.Node content) {
        shell.show(title, content, darkAppearance);
    }

    public void showLogin() {
        signedInUser = null;
        Fxml<LoginController> screen = Fxml.load("Login");
        screen.getController().init(context, this);
        setScreen("University Registration", screen.getRoot());
    }

    public void showBrowse() {
        Fxml<BrowseController> screen = Fxml.load("Browse");
        screen.getController().init(context, this);
        setScreen("Browse Sections", screen.getRoot());
    }

    public void signedIn(User user) {
        signedInUser = user;
        if (user instanceof Student) {
            Fxml<StudentController> screen = Fxml.load("Student");
            screen.getController().init(context, this, (Student) user);
            setScreen("Student", screen.getRoot());
        } else if (user instanceof Instructor) {
            Fxml<InstructorController> screen = Fxml.load("Instructor");
            screen.getController().init(context, this, (Instructor) user);
            setScreen("Instructor", screen.getRoot());
        } else if (user instanceof AcademicStaff) {
            Fxml<StaffController> screen = Fxml.load("Staff");
            screen.getController().init(context, this, (AcademicStaff) user);
            setScreen("Academic Staff", screen.getRoot());
        }
    }

    public void signOut() {
        showLogin();
    }

    public User getSignedInUser() {
        return signedInUser;
    }
}
