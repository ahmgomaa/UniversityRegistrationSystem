package ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import model.CourseSection;
import model.Enrollment;
import model.Student;
import model.enums.EnrollmentStatus;

import java.util.ArrayList;

public class StudentHomeController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label identityLabel;

    @FXML
    private Label standingLabel;

    @FXML
    private Label cgpaValue;

    @FXML
    private Label creditsValue;

    @FXML
    private ProgressBar creditsProgress;

    @FXML
    private Label unreadValue;

    @FXML
    private ListView<String> coursesList;

    private StudentController parent;

    public void init(AppContext context, Student student, StudentController parent) {
        this.parent = parent;

        int unread = context.getNotifications().getInbox(student).size();
        double cgpa = student.getAcademicRecord().calculateCGPA();
        int earned = student.getAcademicRecord().getEarnedCredits();
        int required = student.getProgram().getTotalCreditsRequired();

        titleLabel.setText("Welcome, " + student.getName());
        identityLabel.setText(student.getName() + "  •  " + student.getUserId()
                + "  •  " + student.getProgram().getName());
        standingLabel.setText("Academic status: " + student.getAcademicStatus());
        cgpaValue.setText(UiKit.formatGpa(cgpa));
        creditsValue.setText(earned + " / " + required);
        creditsProgress.setProgress(required == 0 ? 0 : (double) earned / required);
        unreadValue.setText(String.valueOf(unread));
        unreadValue.getStyleClass().add(unread > 0 ? "orange-text" : "gray-text");

        ArrayList<String> active = activeEnrollmentLines(student);
        if (active.isEmpty()) {
            active.add("You are not registered in any course yet.");
        }
        UiKit.prepareList(coursesList);
        coursesList.setItems(FXCollections.observableArrayList(active));
    }

    @FXML
    private void onRegister() {
        parent.showRegister();
    }

    private ArrayList<String> activeEnrollmentLines(Student student) {
        ArrayList<String> lines = new ArrayList<>();
        for (Enrollment enrollment : student.getAcademicRecord().getEnrollments()) {
            if (enrollment.getStatus() == EnrollmentStatus.ENROLLED) {
                CourseSection section = enrollment.getCourseSection();
                lines.add(section.getCourse().getCourseCode() + " — "
                        + section.getCourse().getCourseName()
                        + " (Section " + section.getSectionNo() + ")"
                        + "  •  " + section.getCourse().getCreditHours() + " credits");
            }
        }
        return lines;
    }
}
