package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import model.Student;

import java.util.ArrayList;

public class StudentController {

    @FXML
    private BorderPane studentRoot;

    @FXML
    private Button navHome;

    @FXML
    private Button navCourses;

    @FXML
    private Button navRegister;

    @FXML
    private Button navTranscript;

    @FXML
    private Button navGraduation;

    @FXML
    private Button navNotifications;

    @FXML
    private Button navSettings;

    private AppContext context;
    private JavaFxApp app;
    private Student student;
    private final ArrayList<Button> navButtons = new ArrayList<>();

    public void init(AppContext context, JavaFxApp app, Student student) {
        this.context = context;
        this.app = app;
        this.student = student;
        navButtons.add(navHome);
        navButtons.add(navCourses);
        navButtons.add(navRegister);
        navButtons.add(navTranscript);
        navButtons.add(navGraduation);
        navButtons.add(navNotifications);
        navButtons.add(navSettings);
        showHome();
    }

    private void select(Button selected) {
        for (Button button : navButtons) {
            button.getStyleClass().remove("nav-button-selected");
        }
        selected.getStyleClass().add("nav-button-selected");
    }

    @FXML
    public void onHome() {
        showHome();
    }

    @FXML
    private void onCourses() {
        showCourses();
    }

    @FXML
    public void onRegister() {
        showRegister();
    }

    @FXML
    private void onTranscript() {
        showTranscript();
    }

    @FXML
    private void onGraduation() {
        showGraduation();
    }

    @FXML
    private void onNotifications() {
        showNotifications();
    }

    @FXML
    private void onSettings() {
        showSettings();
    }

    @FXML
    private void onSignOut() {
        app.signOut();
    }

    public void showHome() {
        select(navHome);
        Fxml<StudentHomeController> screen = Fxml.load("StudentHome");
        screen.getController().init(context, student, this);
        studentRoot.setCenter(screen.getRoot());
    }

    public void showCourses() {
        select(navCourses);
        Fxml<StudentCoursesController> screen = Fxml.load("StudentCourses");
        screen.getController().init(context, student);
        studentRoot.setCenter(screen.getRoot());
    }

    public void showRegister() {
        select(navRegister);
        Fxml<StudentRegisterController> screen = Fxml.load("StudentRegister");
        screen.getController().init(context, student, this);
        studentRoot.setCenter(screen.getRoot());
    }

    public void showTranscript() {
        select(navTranscript);
        Fxml<StudentTranscriptController> screen = Fxml.load("StudentTranscript");
        screen.getController().init(context, student);
        studentRoot.setCenter(screen.getRoot());
    }

    public void showGraduation() {
        select(navGraduation);
        Fxml<StudentGraduationController> screen = Fxml.load("StudentGraduation");
        screen.getController().init(student, context);
        studentRoot.setCenter(screen.getRoot());
    }

    public void showNotifications() {
        select(navNotifications);
        Fxml<StudentNotificationsController> screen = Fxml.load("StudentNotifications");
        screen.getController().init(context, student);
        studentRoot.setCenter(screen.getRoot());
    }

    public void showSettings() {
        select(navSettings);
        Fxml<StudentSettingsController> screen = Fxml.load("StudentSettings");
        screen.getController().init(context, app, student);
        studentRoot.setCenter(screen.getRoot());
    }
}
