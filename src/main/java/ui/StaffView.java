package ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

    public BorderPane build() {
        root = new BorderPane();

        VBox nav = new VBox(6);
        nav.setPadding(new Insets(16, 10, 16, 16));
        nav.setPrefWidth(180);
        nav.setStyle("-fx-background-color: white;"
                + "-fx-border-color: transparent " + UiKit.SEPARATOR
                + " transparent transparent;"
                + "-fx-border-width: 0 1 0 0;");
        nav.getChildren().addAll(
                navItem("staff-nav-students", "Students", new Runnable() {
                    @Override
                    public void run() {
                        showStudents();
                    }
                }),
                navItem("staff-nav-status", "Update Status", new Runnable() {
                    @Override
                    public void run() {
                        showStatusPane();
                    }
                }),
                navItem("staff-nav-advisor", "Assign Advisor", new Runnable() {
                    @Override
                    public void run() {
                        showAdvisorPane();
                    }
                }),
                navItem("staff-nav-roster", "Section Roster", new Runnable() {
                    @Override
                    public void run() {
                        showRosterPane();
                    }
                }));
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        nav.getChildren().add(spacer);
        Button signOut = UiKit.plainButton("Sign Out");
        signOut.setId("staff-signout");
        signOut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                app.signOut();
            }
        });
        nav.getChildren().add(signOut);

        root.setLeft(nav);
        showStudents();
        return root;
    }

    private Button navItem(String id, String text, Runnable action) {
        Button button = UiKit.navButton(text);
        button.setId(id);
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                action.run();
            }
        });
        navButtons.put(text, button);
        return button;
    }

    private void updateNavSelection() {
        for (java.util.Map.Entry<String, Button> entry : navButtons.entrySet()) {
            UiKit.setNavSelected(entry.getValue(), entry.getKey().equals(current));
        }
    }

    private VBox screen(String title, javafx.scene.Node... children) {
        VBox box = new VBox(12, UiKit.title(title));
        box.getChildren().addAll(children);
        box.setPadding(new Insets(20));
        UiKit.applyGroupedBackground(box);
        return box;
    }

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
        UiKit.styleInsetList(list);
        list.setId("staff-students-list");
        Label detail = new Label();
        detail.setWrapText(true);
        VBox actions = new VBox(12);

        list.getSelectionModel().selectedIndexProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> obs,
                            Number oldIndex, Number newIndex) {
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
                        statusButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent e) {
                                showStatusPane();
                            }
                        });
                        Button advisorButton = UiKit.plainButton("Assign Advisor");
                        advisorButton.setId("staff-student-advisor-button");
                        advisorButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent e) {
                                showAdvisorPane();
                            }
                        });
                        actions.getChildren().addAll(
                                UiKit.sectionLabel("Actions for " + selectedStudent.getName()),
                                statusButton, advisorButton);
                    }
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
        updateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
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
            }
        });
        center.getChildren().addAll(updateButton, status);
        root.setCenter(center);
    }

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
        assignButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
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
            }
        });
        center.getChildren().addAll(assignButton, status);
        root.setCenter(center);
    }

    private void showRosterPane() {
        current = "Section Roster";
        updateNavSelection();

        ComboBox<CourseSection> sectionBox = new ComboBox<>(
                FXCollections.observableArrayList(context.getStore().getSections()));
        sectionBox.setId("staff-section-box");
        sectionBox.setMaxWidth(Double.MAX_VALUE);
        ListView<String> rosterList = new ListView<>();
        UiKit.styleInsetList(rosterList);
        rosterList.setId("staff-roster-list");
        Label status = UiKit.statusLabel();

        sectionBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
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
            }
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

