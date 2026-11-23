package ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import model.AcademicStaff;
import model.CourseSection;
import model.Student;
import model.enums.AcademicStatus;
import model.enums.StaffRole;
import service.GradingService.RosterEntry;

import java.util.ArrayList;

/**
 * Academic staff portal: view all students, update a student's academic
 * status (registrar), assign an advisor (advisor), view a section roster
 * (registrar). The role checks live in the model and services.
 */
public class StaffView {

    private final AppContext context;
    private final JavaFxApp app;
    private final AcademicStaff staff;

    public StaffView(AppContext context, JavaFxApp app, AcademicStaff staff) {
        this.context = context;
        this.app = app;
        this.staff = staff;
    }

    public TabPane build() {
        TabPane tabs = new TabPane();
        Tab studentsTab = new Tab("Students");
        studentsTab.setContent(studentsPane());
        Tab statusTab = new Tab("Update Status");
        statusTab.setContent(statusPane());
        Tab advisorTab = new Tab("Assign Advisor");
        advisorTab.setContent(advisorPane());
        Tab rosterTab = new Tab("Section Roster");
        rosterTab.setContent(rosterPane());
        tabs.getTabs().addAll(studentsTab, statusTab, advisorTab, rosterTab);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabs;
    }

    private ComboBox<Student> studentBox() {
        ComboBox<Student> box = new ComboBox<>(
                FXCollections.observableArrayList(context.getStore().getStudents()));
        box.setMaxWidth(Double.MAX_VALUE);
        box.setCellFactory(list -> new javafx.scene.control.ListCell<Student>() {
            @Override
            protected void updateItem(Student student, boolean empty) {
                super.updateItem(student, empty);
                setText(empty || student == null ? null
                        : student.getName() + " (" + student.getUserId() + ")");
            }
        });
        box.setButtonCell(new javafx.scene.control.ListCell<Student>() {
            @Override
            protected void updateItem(Student student, boolean empty) {
                super.updateItem(student, empty);
                setText(empty || student == null ? null : student.getName());
            }
        });
        return box;
    }

    private VBox studentsPane() {
        ArrayList<String> lines = new ArrayList<>();
        for (Student student : context.getStore().getStudents()) {
            String advisor = student.getAdvisor() == null
                    ? "No advisor" : student.getAdvisor().getName();
            lines.add(student.getName() + " (" + student.getUserId() + ")"
                    + "  •  " + student.getAcademicStatus()
                    + "  •  CGPA " + UiKit.formatGpa(
                            student.getAcademicRecord().calculateCGPA())
                    + "  •  " + student.getProgram().getName()
                    + "  •  Advisor: " + advisor);
        }
        if (lines.isEmpty()) {
            lines.add("No students.");
        }
        ListView<String> list = new ListView<>(FXCollections.observableArrayList(lines));
        list.setPrefHeight(420);
        Button signOut = UiKit.plainButton("Sign Out");
        signOut.setOnAction(e -> app.signOut());
        VBox root = new VBox(12, UiKit.sectionLabel("All students"), list, signOut);
        root.setPadding(new Insets(20));
        return root;
    }

    private VBox statusPane() {
        ComboBox<Student> studentBox = studentBox();
        ComboBox<AcademicStatus> statusBox = new ComboBox<>(
                FXCollections.observableArrayList(AcademicStatus.values()));
        statusBox.setMaxWidth(Double.MAX_VALUE);
        Label message = new Label();
        message.setWrapText(true);

        Button updateButton = UiKit.primaryButton("Update Status");
        updateButton.setOnAction(e -> {
            Student student = studentBox.getValue();
            AcademicStatus status = statusBox.getValue();
            if (student == null || status == null) {
                message.setText("Select a student and a status first.");
                return;
            }
            try {
                student.updateAcademicStatus(staff, status);
                context.getStore().save();
                message.setText("Updated status for " + student.getName() + " to " + status + ".");
            } catch (IllegalArgumentException ex) {
                message.setText(ex.getMessage());
            }
        });

        VBox root = new VBox(12,
                UiKit.sectionLabel("Student"), studentBox,
                UiKit.sectionLabel("New status (registrar only)"), statusBox,
                updateButton, message);
        root.setPadding(new Insets(20));
        return root;
    }

    private VBox advisorPane() {
        ComboBox<Student> studentBox = studentBox();
        ArrayList<AcademicStaff> advisors = new ArrayList<>();
        for (AcademicStaff member : context.getStore().getStaff()) {
            if (member.hasRole(StaffRole.ADVISOR)) {
                advisors.add(member);
            }
        }
        ComboBox<AcademicStaff> advisorBox = new ComboBox<>(
                FXCollections.observableArrayList(advisors));
        advisorBox.setMaxWidth(Double.MAX_VALUE);
        Label message = new Label();
        message.setWrapText(true);

        Button assignButton = UiKit.primaryButton("Assign Advisor");
        assignButton.setOnAction(e -> {
            Student student = studentBox.getValue();
            AcademicStaff advisor = advisorBox.getValue();
            if (student == null || advisor == null) {
                message.setText("Select a student and an advisor first.");
                return;
            }
            try {
                student.assignAdvisor(advisor);
                context.getStore().save();
                message.setText("Assigned " + advisor.getName()
                        + " to " + student.getName() + ".");
            } catch (IllegalArgumentException ex) {
                message.setText(ex.getMessage());
            }
        });

        VBox root = new VBox(12,
                UiKit.sectionLabel("Student"), studentBox,
                UiKit.sectionLabel("Advisor"), advisorBox,
                assignButton, message);
        root.setPadding(new Insets(20));
        return root;
    }

    private VBox rosterPane() {
        ComboBox<CourseSection> sectionBox = new ComboBox<>(
                FXCollections.observableArrayList(context.getStore().getSections()));
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

        sectionBox.setOnAction(e -> {
            CourseSection section = sectionBox.getValue();
            ArrayList<String> lines = new ArrayList<>();
            if (section != null) {
                try {
                    for (RosterEntry entry : context.getGrading().getRoster(staff, section)) {
                        Student student = entry.getStudent();
                        String grade = entry.getEnrollment().getGrade() == null
                                ? entry.getEnrollment().getStatus().toString()
                                : entry.getEnrollment().getGrade().toString();
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
        });

        VBox root = new VBox(12,
                UiKit.sectionLabel("Section (registrar only)"), sectionBox,
                UiKit.sectionLabel("Roster"), rosterList, message);
        root.setPadding(new Insets(20));
        return root;
    }
}
