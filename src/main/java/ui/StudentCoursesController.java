package ui;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import model.CourseSection;
import model.Enrollment;
import model.Student;
import model.enums.EnrollmentStatus;
import service.RegistrationException;

import java.time.LocalDate;
import java.util.ArrayList;

public class StudentCoursesController {

    @FXML
    private Label emptyLabel;

    @FXML
    private VBox content;

    @FXML
    private ScrollPane coursesListScroll;

    private RowTable coursesList;

    @FXML
    private Label statusLabel;

    private AppContext context;
    private Student student;
    private ArrayList<Enrollment> enrollments;

    public void init(AppContext context, Student student) {
        coursesList = new RowTable(coursesListScroll);
        this.context = context;
        this.student = student;
        coursesList.setColumns(
                new String[] {"CODE", "COURSE", "SECTION", "STATUS", "GRADE"},
                new double[] {2, 5, 2, 2, 2});
        show(null, false);
    }

    private void show(String message, boolean ok) {
        enrollments = student.getAcademicRecord().getEnrollments();
        coursesList.clear();
        for (Enrollment enrollment : enrollments) {
            CourseSection section = enrollment.getCourseSection();
            String grade = enrollment.getGrade() == null
                    ? "-" : enrollment.getGrade().toString();
            coursesList.addRow(new String[] {section.getCourse().getCourseCode(),
                    section.getCourse().getCourseName(),
                    String.valueOf(section.getSectionNo()),
                    enrollment.getStatus().toString(), grade});
        }
        UiKit.clearStatus(statusLabel);
        if (message != null) {
            UiKit.showStatus(statusLabel, ok, message);
        }
        boolean empty = enrollments.isEmpty();
        emptyLabel.setVisible(empty);
        emptyLabel.setManaged(empty);
        content.setVisible(!empty);
        content.setManaged(!empty);
    }

    @FXML
    private void onWithdraw() {
        int index = coursesList.getSelectedIndex();
        if (index < 0 || index >= enrollments.size()) {
            UiKit.showStatus(statusLabel, false, "Select a course first.");
            return;
        }
        Enrollment enrollment = enrollments.get(index);
        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            UiKit.showStatus(statusLabel, false,
                    "Only an active enrollment can be withdrawn.");
            return;
        }
        String code = enrollment.getCourseSection().getCourse().getCourseCode();
        String name = enrollment.getCourseSection().getCourse().getCourseName();
        if (!UiKit.confirm("Confirm withdrawal",
                "Withdraw from " + code + " — " + name + ", Section "
                        + enrollment.getCourseSection().getSectionNo()
                        + "?\nYour seat is released. This cannot be undone from the app.")) {
            return;
        }
        try {
            context.getRegistration().withdraw(student, enrollment, LocalDate.now());
            if (SaveCheck.enrollmentSaved(student.getUserId(),
                    enrollment.getCourseSection().getSectionId(), "WITHDRAWN")) {
                show("Withdrawn from " + code + ". Your seat has been released.", true);
            } else {
                show("The change was not saved. Try again — if it keeps "
                        + "failing, restart the app or contact the registrar.", false);
            }
        } catch (RegistrationException ex) {
            UiKit.showStatus(statusLabel, false, ruleText("Withdrawal failed", ex));
        }
    }

    static String ruleText(String heading, RegistrationException ex) {
        StringBuilder text = new StringBuilder(heading + ":\n");
        for (String error : ex.getErrors()) {
            text.append("• ").append(error).append("\n");
        }
        return text.toString();
    }
}
