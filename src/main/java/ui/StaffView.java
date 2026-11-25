package ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.AcademicStaff;
import model.CourseSection;
import model.Student;
import model.enums.AcademicStatus;
import model.enums.StaffRole;
import service.GradingService.RosterEntry;

import java.util.ArrayList;

/**
 * Academic staff portal, one panel at a time: pick a task in the side
 * navigation, then pick a student, then the action for that student
 * appears. Nothing else is on screen. Every panel is built fresh
 * from the store and services when it is opened, and every update
 * is confirmed against the database (see SaveCheck).
 *
 * The role checks live in the model and services (only a REGISTRAR
 * changes status or views a roster, only an ADVISOR can be assigned).
 *
 * Controls, layouts and styling are the ones from Lecture 7/8:
 * BorderPane/VBox, Label/Button/ComboBox/ListView of plain strings,
 * setOnAction handlers and inline CSS.
 */
public class StaffView {

    private final AppContext context;
    private final JavaFxApp app;
    private final AcademicStaff staff;

    private BorderPane root;
    private String current = "Students";
    private Student selectedStudent;
    private final java.util.Map<String, Button> navButtons =
            new java.util.LinkedHashMap<>();

    public StaffView(AppContext context, JavaFxApp app, AcademicStaff staff) {
        this.context = context;
        this.app = app;
        this.staff = staff;
    }

    /** Side navigation on the left, the open panel in the centre. */
    public BorderPane build() {
        root = new BorderPane();

        VBox nav = new VBox(6);
        nav.setPadding(new Insets(16, 10, 16, 16));
        nav.setPrefWidth(180);
        nav.getChildren().addAll(
                navItem("staff-nav-students", "Students", this::showStudents),
                navItem("staff-nav-status", "Update Status", this::showStatusPane),
                navItem("staff-nav-advisor", "Assign Advisor", this::showAdvisorPane),
                navItem("staff-nav-roster", "Section Roster", this::showRosterPane));
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        nav.getChildren().add(spacer);
        Button signOut = UiKit.plainButton("Sign Out");
        signOut.setId("staff-signout");
        signOut.setOnAction(e -> app.signOut());
        nav.getChildren().add(signOut);

        root.setLeft(nav);
        showStudents();
        return root;
    }

    private Button navItem(String id, String text, Runnable action) {
        Button button = UiKit.navButton(text);
        button.setId(id);
        button.setOnAction(e -> action.run());
        navButtons.put(text, button);
        return button;
    }

    /** Highlight the open panel in the side navigation. */
    private void updateNavSelection() {
        for (java.util.Map.Entry<String, Button> entry : navButtons.entrySet()) {
            UiKit.setNavSelected(entry.getValue(), entry.getKey().equals(current));
        }
    }

    private VBox screen(String title, javafx.scene.Node... children) {
        VBox box = new VBox(12, UiKit.title(title));
        box.getChildren().addAll(children);
        box.setPadding(new Insets(20));
        return box;
    }

    // --------------------------------------------------------------- Students

    /** All students, fresh. Pick one to see what can be done for them. */
    private void showStudents() {
        current = "Students";
        updateNavSelection();
        selectedStudent = null;

        ArrayList<String> lines = new ArrayList<>();
        for (Student student : context.getStore().getStudents()) {
            lines.add(describe(student));
        }
        if (lines.isEmpty()) {
            lines.add("No students.");
        }
        ListView<String> list = new ListView<>(FXCollections.observableArrayList(lines));
        list.setId("staff-students-list");
        list.setPrefHeight(300);
        Label detail = new Label();
        detail.setWrapText(true);
        VBox actions = new VBox(12);

        list.getSelectionModel().selectedIndexProperty().addListener(
                (obs, oldIndex, newIndex) -> {
                    int index = newIndex.intValue();
                    ArrayList<Student> students = context.getStore().getStudents();
                    if (index < 0 || index >= students.size()) {
                        return;
                    }
                    selectedStudent = students.get(index);
                    detail.setText(describe(selectedStudent)
                            + "\n\nAdvisor: "
                            + (selectedStudent.getAdvisor() == null
                                    ? "None" : selectedStudent.getAdvisor().getName()));
                    actions.getChildren().clear();
                    Button statusButton = UiKit.plainButton("Update Status");
                    statusButton.setId("staff-student-status-button");
                    statusButton.setOnAction(e -> showStatusPane());
                    Button advisorButton = UiKit.plainButton("Assign Advisor");
                    advisorButton.setId("staff-student-advisor-button");
                    advisorButton.setOnAction(e -> showAdvisorPane());
                    actions.getChildren().addAll(
                            UiKit.sectionLabel("Actions for " + selectedStudent.getName()),
                            statusButton, advisorButton);
                });

        VBox center = screen("All students", list, detail, actions);
        root.setCenter(center);
    }

    private String describe(Student student) {
        String advisor = student.getAdvisor() == null
                ? "No advisor" : student.getAdvisor().getName();
        return student.getName() + " (" + student.getUserId() + ")"
                + "  •  " + student.getAcademicStatus()
                + "  •  CGPA " + UiKit.formatGpa(
                        student.getAcademicRecord().calculateCGPA())
                + "  •  " + student.getProgram().getName()
                + "  •  Advisor: " + advisor;
    }

    // ------------------------------------------------------------ Update status

    /** Pick a student first; the status choice appears only then. */
    private void showStatusPane() {
        current = "Update Status";
        updateNavSelection();

        ComboBox<Student> studentBox = studentBox();
        studentBox.setId("staff-student-box");
        if (selectedStudent != null) {
            studentBox.setValue(selectedStudent);
        }
        ComboBox<AcademicStatus> statusBox = new ComboBox<>(
                FXCollections.observableArrayList(AcademicStatus.values()));
        statusBox.setId("staff-status-box");
        statusBox.setMaxWidth(Double.MAX_VALUE);
        Label status = UiKit.statusLabel();

        VBox center = screen("Update status",
                UiKit.sectionLabel("Student"), studentBox,
                UiKit.sectionLabel("New status (registrar only)"), statusBox);
        Button updateButton = UiKit.primaryButton("Update Status");
        updateButton.setId("staff-update-button");
        updateButton.setOnAction(e -> {
            Student student = studentBox.getValue();
            AcademicStatus newStatus = statusBox.getValue();
            if (student == null || newStatus == null) {
                UiKit.showStatus(status, false, "Select a student and a status first.");
                return;
            }
            try {
                student.updateAcademicStatus(staff, newStatus);
                context.getStore().save();
                String advisorId = student.getAdvisor() == null
                        ? null : student.getAdvisor().getUserId();
                if (SaveCheck.studentSaved(student.getUserId(),
                        newStatus.name(), advisorId)) {
                    UiKit.showStatus(status, true, "Updated status for "
                            + student.getName() + " to " + newStatus + ".");
                } else {
                    UiKit.showStatus(status, false,
                            "The change was not saved. Try again — if it keeps "
                                    + "failing, restart the app or contact the registrar.");
                }
            } catch (IllegalArgumentException ex) {
                UiKit.showStatus(status, false, ex.getMessage());
            }
        });
        center.getChildren().addAll(updateButton, status);
        root.setCenter(center);
    }

    // ------------------------------------------------------------ Assign advisor

    /** Pick a student first; the advisor choice appears only then. */
    private void showAdvisorPane() {
        current = "Assign Advisor";
        updateNavSelection();

        ComboBox<Student> studentBox = studentBox();
        studentBox.setId("staff-student-box");
        if (selectedStudent != null) {
            studentBox.setValue(selectedStudent);
        }
        ArrayList<AcademicStaff> advisors = new ArrayList<>();
        for (AcademicStaff member : context.getStore().getStaff()) {
            if (member.hasRole(StaffRole.ADVISOR)) {
                advisors.add(member);
            }
        }
        ComboBox<AcademicStaff> advisorBox = new ComboBox<>(
                FXCollections.observableArrayList(advisors));
        advisorBox.setId("staff-advisor-box");
        advisorBox.setMaxWidth(Double.MAX_VALUE);
        Label status = UiKit.statusLabel();

        VBox center = screen("Assign advisor",
                UiKit.sectionLabel("Student"), studentBox,
                UiKit.sectionLabel("Advisor"), advisorBox);
        Button assignButton = UiKit.primaryButton("Assign Advisor");
        assignButton.setId("staff-assign-button");
        assignButton.setOnAction(e -> {
            Student student = studentBox.getValue();
            AcademicStaff advisor = advisorBox.getValue();
            if (student == null || advisor == null) {
                UiKit.showStatus(status, false, "Select a student and an advisor first.");
                return;
            }
            try {
                student.assignAdvisor(advisor);
                context.getStore().save();
                if (SaveCheck.studentSaved(student.getUserId(),
                        student.getAcademicStatus().name(), advisor.getUserId())) {
                    UiKit.showStatus(status, true, "Assigned " + advisor.getName()
                            + " to " + student.getName() + ".");
                } else {
                    UiKit.showStatus(status, false,
                            "The change was not saved. Try again — if it keeps "
                                    + "failing, restart the app or contact the registrar.");
                }
            } catch (IllegalArgumentException ex) {
                UiKit.showStatus(status, false, ex.getMessage());
            }
        });
        center.getChildren().addAll(assignButton, status);
        root.setCenter(center);
    }

    // ------------------------------------------------------------ Section roster

    /** Pick a section first; its roster appears only then. */
    private void showRosterPane() {
        current = "Section Roster";
        updateNavSelection();

        ComboBox<CourseSection> sectionBox = new ComboBox<>(
                FXCollections.observableArrayList(context.getStore().getSections()));
        sectionBox.setId("staff-section-box");
        sectionBox.setMaxWidth(Double.MAX_VALUE);
        ListView<String> rosterList = new ListView<>();
        rosterList.setId("staff-roster-list");
        rosterList.setPrefHeight(380);
        Label status = UiKit.statusLabel();

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
                    UiKit.showStatus(status, false, ex.getMessage());
                }
            }
            if (lines.isEmpty()) {
                lines.add("No students in this section yet.");
            }
            rosterList.setItems(FXCollections.observableArrayList(lines));
        });

        root.setCenter(screen("Section roster",
                UiKit.sectionLabel("Section (registrar only)"), sectionBox,
                UiKit.sectionLabel("Roster"), rosterList, status));
    }

    private ComboBox<Student> studentBox() {
        ComboBox<Student> box = new ComboBox<>(
                FXCollections.observableArrayList(context.getStore().getStudents()));
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }
}
