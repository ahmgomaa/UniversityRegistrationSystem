package ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
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

/**
 * Instructor portal, one task at a time: pick a section, see its
 * roster, pick a student, then record the grade. Each step appears
 * only when the previous one is done, and the roster is rebuilt fresh
 * from GradingService after every grade is recorded.
 *
 * Grading goes through GradingService — it completes the enrollment,
 * notifies the student and updates the academic standing — and the
 * result is confirmed against the database (see SaveCheck).
 *
 * Controls, layouts and styling are the ones from Lecture 7/8:
 * VBox, Label, Button, ComboBox, ListView of plain strings,
 * TextField, setOnAction handlers and inline CSS.
 */
public class InstructorView {

    private final AppContext context;
    private final JavaFxApp app;
    private final Instructor instructor;

    private CourseSection section;
    private ArrayList<RosterEntry> roster = new ArrayList<>();
    private RosterEntry selectedEntry;
    private int selectedIndex = -1;
    private VBox root;
    private Label status;

    public InstructorView(AppContext context, JavaFxApp app, Instructor instructor) {
        this.context = context;
        this.app = app;
        this.instructor = instructor;
    }

    public VBox build() {
        root = new VBox(12);
        root.setPadding(new Insets(20));
        status = UiKit.statusLabel();
        render();
        return root;
    }

    private ArrayList<CourseSection> mySections() {
        ArrayList<CourseSection> mine = new ArrayList<>();
        for (CourseSection section : context.getStore().getSections()) {
            if (section.getInstructor().equals(instructor)) {
                mine.add(section);
            }
        }
        return mine;
    }

    /** Rebuild the whole screen from current state (fresh from backend). */
    private void render() {
        root.getChildren().clear();
        root.getChildren().add(UiKit.sectionLabel("Record a grade"));

        // First section is the default, so its roster shows right away.
        if (section == null && !mySections().isEmpty()) {
            section = mySections().get(0);
            loadRoster();
        }

        // Step 1: pick one of my sections.
        ComboBox<CourseSection> sectionBox = new ComboBox<>(
                FXCollections.observableArrayList(mySections()));
        sectionBox.setId("instructor-section-box");
        sectionBox.setMaxWidth(Double.MAX_VALUE);
        if (section != null) {
            sectionBox.setValue(section);
        } else if (!mySections().isEmpty()) {
            sectionBox.setValue(mySections().get(0));
        }
        sectionBox.setOnAction(e -> {
            section = sectionBox.getValue();
            selectedEntry = null;
            loadRoster();
            selectedIndex = -1;
            render();
        });
        root.getChildren().addAll(UiKit.sectionLabel("Section"), sectionBox);

        // Step 2: the roster of the chosen section (fresh from the service).
        if (section != null) {
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
            ListView<String> rosterList = new ListView<>(
                    FXCollections.observableArrayList(lines));
            rosterList.setId("instructor-roster-list");
            rosterList.setPrefHeight(260);
            // Keep the clicked row highlighted after the rebuild (A2).
            if (selectedIndex >= 0 && selectedIndex < roster.size()) {
                rosterList.getSelectionModel().select(selectedIndex);
            }
            rosterList.getSelectionModel().selectedIndexProperty().addListener(
                    (obs, oldIndex, newIndex) -> {
                        int index = newIndex.intValue();
                        selectedIndex = index;
                        selectedEntry = (index >= 0 && index < roster.size())
                                ? roster.get(index) : null;
                        render();
                    });
            root.getChildren().addAll(UiKit.sectionLabel("Roster"), rosterList);
        }

        // Step 3: record a grade for the chosen student — nothing else.
        // Only an active enrollment can be graded; a completed one
        // just shows its grade, there is nothing to record.
        if (selectedEntry != null) {
            Student student = selectedEntry.getStudent();
            Enrollment enrollment = selectedEntry.getEnrollment();
            if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
                Label done = new Label(student.getName() + " is already graded: "
                        + enrollment.getGrade() + ". Nothing to record.");
                root.getChildren().addAll(UiKit.sectionLabel("Record grade"), done);
            } else {
                Label who = new Label("Grade for " + student.getName()
                        + " (" + student.getUserId() + ") in "
                        + section.getCourse().getCourseCode() + ".");
                TextField scoreField = new TextField();
                scoreField.setId("instructor-score-field");
                scoreField.setPromptText("Score (0–100)");
                Button recordButton = UiKit.primaryButton("Record Grade");
                recordButton.setId("instructor-record-button");
                recordButton.setOnAction(e -> recordGrade(scoreField.getText()));
                VBox form = UiKit.card(who, scoreField, recordButton);
                root.getChildren().addAll(UiKit.sectionLabel("Record grade"), form);
            }
        }

        root.getChildren().add(status);

        Button signOut = UiKit.plainButton("Sign Out");
        signOut.setId("instructor-signout");
        signOut.setOnAction(e -> app.signOut());
        root.getChildren().add(signOut);
    }

    private void loadRoster() {
        roster = new ArrayList<>();
        if (section == null) {
            return;
        }
        try {
            roster = context.getGrading().getRoster(instructor, section);
        } catch (IllegalArgumentException ex) {
            UiKit.showStatus(status, false, ex.getMessage());
        }
    }

    private void recordGrade(String scoreText) {
        int score;
        try {
            score = Integer.parseInt(scoreText.trim());
        } catch (NumberFormatException ex) {
            UiKit.showStatus(status, false, "Score must be a number from 0 to 100.");
            return;
        }
        if (score < 0 || score > 100) {
            UiKit.showStatus(status, false, "Score must be between 0 and 100.");
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
            UiKit.showStatus(status, false,
                    "Only an active enrollment can be graded.");
            return;
        }
        try {
            context.getGrading().recordGrade(instructor, selectedEntry.getStudent(),
                    selectedEntry.getEnrollment(), score);
            Student student = selectedEntry.getStudent();
            if (SaveCheck.gradeSaved(student.getUserId(),
                    section.getSectionId(), score)) {
                UiKit.showStatus(status, true,
                        "Grade recorded for " + student.getName() + ": " + score + ".");
            } else {
                UiKit.showStatus(status, false,
                        "The change was not saved. Try again — if it keeps "
                                + "failing, restart the app or contact the registrar.");
            }
            selectedEntry = null;
            selectedIndex = -1;
            loadRoster(); // fresh roster: the graded student now shows the grade
            render();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            UiKit.showStatus(status, false, ex.getMessage());
        }
    }
}
