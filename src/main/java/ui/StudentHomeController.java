package ui;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import model.CourseSection;
import model.Enrollment;
import model.Student;
import model.enums.EnrollmentStatus;


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
    private ScrollPane coursesListScroll;

    private RowTable coursesList;

    private StudentController parent;

    public void init(AppContext context, Student student, StudentController parent) {
        coursesList = new RowTable(coursesListScroll);
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

        coursesList.setSelectable(false);
        coursesList.setColumns(new String[] {"CODE", "COURSE", "SECTION", "CREDITS"},
                new double[] {2, 6, 2, 2});
        int shown = 0;
        for (Enrollment enrollment : student.getAcademicRecord().getEnrollments()) {
            if (enrollment.getStatus() == EnrollmentStatus.ENROLLED) {
                CourseSection section = enrollment.getCourseSection();
                coursesList.addRow(new String[] {section.getCourse().getCourseCode(),
                        section.getCourse().getCourseName(),
                        String.valueOf(section.getSectionNo()),
                        String.valueOf(section.getCourse().getCreditHours())});
                shown++;
            }
        }
        if (shown == 0) {
            coursesList.showEmpty("You are not registered in any course yet.");
        }
        int tableRows = shown == 0 ? 1 : shown;
        if (tableRows > 6) {
            tableRows = 6;
        }
        double tableHeight = Math.max(120, 40 + 40 * tableRows);
        coursesListScroll.setPrefHeight(tableHeight);
        coursesListScroll.setMaxHeight(tableHeight);
    }

    @FXML
    private void onRegister() {
        parent.showRegister();
    }
}
