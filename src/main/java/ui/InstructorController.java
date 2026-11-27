package ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import model.CourseSection;
import model.Enrollment;
import model.Instructor;
import model.Student;
import model.enums.EnrollmentStatus;
import service.GradingService.RosterEntry;

import java.util.ArrayList;

public class InstructorController {

    @FXML
    private Label studentsValue;

    @FXML
    private Label gradedValue;

    @FXML
    private Label pendingValue;

    @FXML
    private ComboBox<CourseSection> sectionBox;

    @FXML
    private VBox rosterPane;

    @FXML
    private ListView<String> rosterList;

    @FXML
    private VBox gradePane;

    @FXML
    private Label doneLabel;

    @FXML
    private VBox formCard;

    @FXML
    private Label whoLabel;

    @FXML
    private TextField scoreField;

    @FXML
    private Label statusLabel;

    private AppContext context;
    private JavaFxApp app;
    private Instructor instructor;

    private CourseSection section;
    private ArrayList<RosterEntry> roster = new ArrayList<>();
    private RosterEntry selectedEntry;
    private boolean updating;

    public void init(AppContext context, JavaFxApp app, Instructor instructor) {
        this.context = context;
        this.app = app;
        this.instructor = instructor;

        UiKit.prepareList(rosterList);
        rosterList.getSelectionModel().selectedIndexProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> obs,
                            Number oldIndex, Number newIndex) {
                        if (updating) {
                            return;
                        }
                        int index = newIndex.intValue();
                        selectedEntry = (index >= 0 && index < roster.size())
                                ? roster.get(index) : null;
                        showGradePane();
                    }
                });

        ArrayList<CourseSection> mine = mySections();
        updating = true;
        sectionBox.setItems(FXCollections.observableArrayList(mine));
        if (!mine.isEmpty()) {
            section = mine.get(0);
            sectionBox.setValue(section);
            loadRoster();
        }
        updating = false;
        showRoster();
        showGradePane();
    }

    private ArrayList<CourseSection> mySections() {
        ArrayList<CourseSection> mine = new ArrayList<>();
        for (CourseSection candidate : context.getStore().getSections()) {
            if (candidate.getInstructor().equals(instructor)) {
                mine.add(candidate);
            }
        }
        return mine;
    }

    private void showRoster() {
        rosterPane.setVisible(section != null);
        rosterPane.setManaged(section != null);
        int graded = 0;
        int pending = 0;
        for (RosterEntry entry : roster) {
            if (entry.getEnrollment().getStatus() == EnrollmentStatus.ENROLLED) {
                pending++;
            } else {
                graded++;
            }
        }
        studentsValue.setText(String.valueOf(roster.size()));
        gradedValue.setText(String.valueOf(graded));
        pendingValue.setText(String.valueOf(pending));
        if (section == null) {
            return;
        }
        ArrayList<String> lines = new ArrayList<>();
        for (RosterEntry entry : roster) {
            Student student = entry.getStudent();
            Enrollment enrollment = entry.getEnrollment();
            String grade = enrollment.getGrade() == null
                    ? enrollment.getStatus().toString()
                    : enrollment.getGrade().toString();
            lines.add(student.getName() + " (" + student.getUserId() + ")"
                    + "  •  " + grade);
        }
        if (lines.isEmpty()) {
            lines.add("No students in this section yet.");
        }
        updating = true;
        rosterList.setItems(FXCollections.observableArrayList(lines));
        updating = false;
    }

    private void showGradePane() {
        gradePane.setVisible(selectedEntry != null);
        gradePane.setManaged(selectedEntry != null);
        if (selectedEntry == null) {
            return;
        }
        Student student = selectedEntry.getStudent();
        Enrollment enrollment = selectedEntry.getEnrollment();
        boolean graded = enrollment.getStatus() != EnrollmentStatus.ENROLLED;
        doneLabel.setVisible(graded);
        doneLabel.setManaged(graded);
        formCard.setVisible(!graded);
        formCard.setManaged(!graded);
        if (graded) {
            doneLabel.setText(student.getName() + " is already graded: "
                    + enrollment.getGrade() + ". Nothing to record.");
        } else {
            whoLabel.setText("Grade for " + student.getName()
                    + " (" + student.getUserId() + ") in "
                    + section.getCourse().getCourseCode() + ".");
            scoreField.clear();
        }
    }

    @FXML
    private void onSectionChosen() {
        if (updating) {
            return;
        }
        section = sectionBox.getValue();
        selectedEntry = null;
        UiKit.clearStatus(statusLabel);
        loadRoster();
        showRoster();
        showGradePane();
    }

    @FXML
    private void onRecord() {
        recordGrade(scoreField.getText());
    }

    @FXML
    private void onSignOut() {
        app.signOut();
    }

    private void loadRoster() {
        roster = new ArrayList<>();
        if (section == null) {
            return;
        }
        try {
            roster = context.getGrading().getRoster(instructor, section);
        } catch (IllegalArgumentException ex) {
            UiKit.showStatus(statusLabel, false, ex.getMessage());
        }
    }

    private void recordGrade(String scoreText) {
        int score;
        try {
            score = Integer.parseInt(scoreText.trim());
        } catch (NumberFormatException ex) {
            UiKit.showStatus(statusLabel, false, "Score must be a number from 0 to 100.");
            return;
        }
        if (score < 0 || score > 100) {
            UiKit.showStatus(statusLabel, false, "Score must be between 0 and 100.");
            return;
        }
        Student gradedStudent = selectedEntry.getStudent();
        if (!UiKit.confirm("Confirm grade",
                "Record " + score + " for " + gradedStudent.getName() + " in "
                        + section.getCourse().getCourseCode()
                        + "?\nGrades cannot be edited in the app once recorded.")) {
            return;
        }
        if (selectedEntry.getEnrollment().getStatus()
                != EnrollmentStatus.ENROLLED) {
            UiKit.showStatus(statusLabel, false,
                    "Only an active enrollment can be graded.");
            return;
        }
        try {
            context.getGrading().recordGrade(instructor, selectedEntry.getStudent(),
                    selectedEntry.getEnrollment(), score);
            Student student = selectedEntry.getStudent();
            if (SaveCheck.gradeSaved(student.getUserId(),
                    section.getSectionId(), score)) {
                UiKit.showStatus(statusLabel, true,
                        "Grade recorded for " + student.getName() + ": " + score + ".");
            } else {
                UiKit.showStatus(statusLabel, false,
                        "The change was not saved. Try again — if it keeps "
                                + "failing, restart the app or contact the registrar.");
            }
            selectedEntry = null;
            loadRoster();
            showRoster();
            showGradePane();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UiKit.showStatus(statusLabel, false, ex.getMessage());
        }
    }
}
