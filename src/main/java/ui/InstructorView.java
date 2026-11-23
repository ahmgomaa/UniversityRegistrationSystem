package ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import model.CourseSection;
import model.Enrollment;
import model.Instructor;
import model.Student;
import service.GradingService.RosterEntry;

import java.util.ArrayList;

/**
 * Instructor portal: assigned sections, section roster, record a grade.
 * Grading goes through GradingService — it completes the enrollment,
 * notifies the student and updates the academic standing.
 */
public class InstructorView {

    private final AppContext context;
    private final JavaFxApp app;
    private final Instructor instructor;

    public InstructorView(AppContext context, JavaFxApp app, Instructor instructor) {
        this.context = context;
        this.app = app;
        this.instructor = instructor;
    }

    public TabPane build() {
        TabPane tabs = new TabPane();
        Tab sectionsTab = new Tab("My Sections");
        sectionsTab.setContent(sectionsPane());
        Tab gradesTab = new Tab("Record Grade");
        gradesTab.setContent(gradePane());
        tabs.getTabs().addAll(sectionsTab, gradesTab);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabs;
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

    private VBox sectionsPane() {
        ComboBox<CourseSection> sectionBox = new ComboBox<>(
                FXCollections.observableArrayList(mySections()));
        sectionBox.setMaxWidth(Double.MAX_VALUE);
        sectionBox.setCellFactory(list -> new javafx.scene.control.ListCell<CourseSection>() {
            @Override
            protected void updateItem(CourseSection section, boolean empty) {
                super.updateItem(section, empty);
                setText(empty || section == null ? null : BrowseView.describe(section));
            }
        });
        sectionBox.setButtonCell(new javafx.scene.control.ListCell<CourseSection>() {
            @Override
            protected void updateItem(CourseSection section, boolean empty) {
                super.updateItem(section, empty);
                setText(empty || section == null ? null
                        : section.getCourse().getCourseCode() + " — Section "
                                + section.getSectionNo());
            }
        });

        ListView<String> rosterList = new ListView<>();
        rosterList.setPrefHeight(380);
        Label message = new Label();
        message.setWrapText(true);

        Runnable refresh = () -> {
            CourseSection section = sectionBox.getValue();
            ArrayList<String> lines = new ArrayList<>();
            if (section != null) {
                try {
                    for (RosterEntry entry : context.getGrading().getRoster(instructor, section)) {
                        Student student = entry.getStudent();
                        Enrollment enrollment = entry.getEnrollment();
                        String grade = enrollment.getGrade() == null
                                ? enrollment.getStatus().toString()
                                : enrollment.getGrade().toString();
                        lines.add(student.getName() + " (" + student.getUserId() + ")"
                                + "  •  " + grade);
                    }
                } catch (IllegalArgumentException ex) {
                    message.setText(ex.getMessage());
                }
            }
            if (lines.isEmpty()) {
                lines.add("No students in this section yet.");
            }
            rosterList.setItems(FXCollections.observableArrayList(lines));
        };
        sectionBox.setOnAction(e -> refresh.run());
        if (!mySections().isEmpty()) {
            sectionBox.setValue(mySections().get(0));
            refresh.run();
        }

        Button signOut = UiKit.plainButton("Sign Out");
        signOut.setOnAction(e -> app.signOut());

        VBox root = new VBox(12,
                UiKit.sectionLabel("Section"), sectionBox,
                UiKit.sectionLabel("Roster"), rosterList, message, signOut);
        root.setPadding(new Insets(20));
        return root;
    }

    private VBox gradePane() {
        ComboBox<CourseSection> sectionBox = new ComboBox<>(
                FXCollections.observableArrayList(mySections()));
        sectionBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<RosterEntry> studentBox = new ComboBox<>();
        studentBox.setMaxWidth(Double.MAX_VALUE);
        studentBox.setCellFactory(list -> new javafx.scene.control.ListCell<RosterEntry>() {
            @Override
            protected void updateItem(RosterEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                setText(empty || entry == null ? null
                        : entry.getStudent().getName()
                                + " (" + entry.getStudent().getUserId() + ")");
            }
        });
        studentBox.setButtonCell(new javafx.scene.control.ListCell<RosterEntry>() {
            @Override
            protected void updateItem(RosterEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                setText(empty || entry == null ? null : entry.getStudent().getName());
            }
        });

        TextField scoreField = new TextField();
        scoreField.setPromptText("Score (0–100)");
        Label message = new Label();
        message.setWrapText(true);

        sectionBox.setOnAction(e -> {
            CourseSection section = sectionBox.getValue();
            if (section == null) {
                studentBox.setItems(FXCollections.observableArrayList());
            } else {
                studentBox.setItems(FXCollections.observableArrayList(
                        context.getGrading().getRoster(instructor, section)));
            }
        });
        if (!mySections().isEmpty()) {
            sectionBox.setValue(mySections().get(0));
        }

        Button recordButton = UiKit.primaryButton("Record Grade");
        recordButton.setOnAction(e -> {
            CourseSection section = sectionBox.getValue();
            RosterEntry entry = studentBox.getValue();
            if (section == null || entry == null) {
                message.setText("Select a section and a student first.");
                return;
            }
            int score;
            try {
                score = Integer.parseInt(scoreField.getText().trim());
            } catch (NumberFormatException ex) {
                message.setText("Score must be a number from 0 to 100.");
                return;
            }
            try {
                context.getGrading().recordGrade(instructor, entry.getStudent(),
                        entry.getEnrollment(), score);
                message.setText("Grade recorded for " + entry.getStudent().getName()
                        + ": " + score + ".");
            } catch (IllegalArgumentException ex) {
                message.setText(ex.getMessage());
            }
        });

        Button signOut = UiKit.plainButton("Sign Out");
        signOut.setOnAction(e -> app.signOut());

        VBox root = new VBox(12,
                UiKit.sectionLabel("Section"), sectionBox,
                UiKit.sectionLabel("Student"), studentBox,
                UiKit.sectionLabel("Score"), scoreField,
                recordButton, message, signOut);
        root.setPadding(new Insets(20));
        return root;
    }
}
