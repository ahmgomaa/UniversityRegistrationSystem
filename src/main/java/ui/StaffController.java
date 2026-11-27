package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import model.AcademicStaff;
import model.Student;

import java.util.ArrayList;

public class StaffController {

    @FXML
    private BorderPane staffRoot;

    @FXML
    private Button navStudents;

    @FXML
    private Button navStatus;

    @FXML
    private Button navAdvisor;

    @FXML
    private Button navRoster;

    private AppContext context;
    private JavaFxApp app;
    private AcademicStaff staff;
    private Student selectedStudent;
    private final ArrayList<Button> navButtons = new ArrayList<>();

    public void init(AppContext context, JavaFxApp app, AcademicStaff staff) {
        this.context = context;
        this.app = app;
        this.staff = staff;
        navButtons.add(navStudents);
        navButtons.add(navStatus);
        navButtons.add(navAdvisor);
        navButtons.add(navRoster);
        showStudents();
    }

    private void select(Button selected) {
        for (Button button : navButtons) {
            button.getStyleClass().remove("nav-button-selected");
        }
        selected.getStyleClass().add("nav-button-selected");
    }

    public Student getSelectedStudent() {
        return selectedStudent;
    }

    public void setSelectedStudent(Student student) {
        this.selectedStudent = student;
    }

    @FXML
    private void onStudents() {
        showStudents();
    }

    @FXML
    private void onStatus() {
        showStatusPane();
    }

    @FXML
    private void onAdvisor() {
        showAdvisorPane();
    }

    @FXML
    private void onRoster() {
        showRosterPane();
    }

    @FXML
    private void onSignOut() {
        app.signOut();
    }

    public void showStudents() {
        select(navStudents);
        selectedStudent = null;
        Fxml<StaffStudentsController> screen = Fxml.load("StaffStudents");
        screen.getController().init(context, this);
        staffRoot.setCenter(screen.getRoot());
    }

    public void showStatusPane() {
        select(navStatus);
        Fxml<StaffStatusController> screen = Fxml.load("StaffStatus");
        screen.getController().init(context, staff, selectedStudent);
        staffRoot.setCenter(screen.getRoot());
    }

    public void showAdvisorPane() {
        select(navAdvisor);
        Fxml<StaffAdvisorController> screen = Fxml.load("StaffAdvisor");
        screen.getController().init(context, selectedStudent);
        staffRoot.setCenter(screen.getRoot());
    }

    public void showRosterPane() {
        select(navRoster);
        Fxml<StaffRosterController> screen = Fxml.load("StaffRoster");
        screen.getController().init(context, staff);
        staffRoot.setCenter(screen.getRoot());
    }
}
