package ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import model.CourseSection;
import model.Enrollment;
import model.Notification;
import model.Semester;
import model.Student;
import model.enums.EnrollmentStatus;
import service.GraduationService.GraduationReport;
import service.RegistrationException;
import service.ReportService.SemesterSummary;
import service.ReportService.Transcript;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Student portal: dashboard, register, my courses / withdraw, transcript,
 * graduation eligibility, notifications and change password.
 * Every action calls the existing services — nothing is reimplemented here.
 */
public class StudentView {

    private final AppContext context;
    private final JavaFxApp app;
    private final Student student;

    public StudentView(AppContext context, JavaFxApp app, Student student) {
        this.context = context;
        this.app = app;
        this.student = student;
    }

    public TabPane build() {
        TabPane tabs = new TabPane();
        tabs.getTabs().addAll(
                tab("Dashboard", dashboard()),
                tab("Register", registerPane()),
                tab("My Courses", myCoursesPane()),
                tab("Transcript", transcriptPane()),
                tab("Graduation", graduationPane()),
                tab("Notifications", notificationsPane()),
                tab("Settings", settingsPane()));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabs;
    }

    private Tab tab(String title, VBox content) {
        Tab tab = new Tab(title);
        content.setPadding(new Insets(20));
        tab.setContent(content);
        return tab;
    }

    private VBox dashboard() {
        int inboxSize = context.getNotifications().getInbox(student).size();
        String advisor = student.getAdvisor() == null
                ? "None" : student.getAdvisor().getName();
        VBox card = UiKit.card(
                new Label("Welcome, " + student.getName()),
                new Label("ID: " + student.getUserId()
                        + "  •  Student number: " + student.getStudentNumber()),
                new Label("Program: " + student.getProgram().getName()
                        + "  •  Level " + student.getLevel()),
                new Label("Status: " + student.getAcademicStatus()),
                new Label("CGPA: " + UiKit.formatGpa(
                        student.getAcademicRecord().calculateCGPA())
                        + "  •  Earned credits: "
                        + student.getAcademicRecord().getEarnedCredits()
                        + " / " + student.getProgram().getTotalCreditsRequired()),
                new Label("Advisor: " + advisor),
                new Label("Notifications: " + inboxSize + " unread message(s)"));
        Button signOut = UiKit.plainButton("Sign Out");
        signOut.setOnAction(e -> app.signOut());
        return new VBox(12, UiKit.sectionLabel("Dashboard"), card, signOut);
    }

    private VBox registerPane() {
        ArrayList<Semester> semesters = context.getStore().getSemesters();
        ComboBox<Semester> semesterBox = new ComboBox<>(
                FXCollections.observableArrayList(semesters));
        if (!semesters.isEmpty()) {
            semesterBox.setValue(semesters.get(0));
        }
        semesterBox.setMaxWidth(Double.MAX_VALUE);

        ListView<CourseSection> sectionsList = new ListView<>();
        sectionsList.setPrefHeight(300);
        sectionsList.setCellFactory(list -> new javafx.scene.control.ListCell<CourseSection>() {
            @Override
            protected void updateItem(CourseSection section, boolean empty) {
                super.updateItem(section, empty);
                setText(empty || section == null ? null : BrowseView.describe(section));
            }
        });

        Label message = new Label();
        message.setWrapText(true);

        Runnable refresh = () -> {
            Semester semester = semesterBox.getValue();
            if (semester == null) {
                sectionsList.setItems(FXCollections.observableArrayList());
            } else {
                sectionsList.setItems(FXCollections.observableArrayList(
                        context.getStore().getSectionsInSemester(semester)));
            }
        };
        semesterBox.setOnAction(e -> refresh.run());
        refresh.run();

        Button registerButton = UiKit.primaryButton("Register in Selected Section");
        registerButton.setOnAction(e -> {
            CourseSection section = sectionsList.getSelectionModel().getSelectedItem();
            if (section == null) {
                message.setText("Select a section first.");
                return;
            }
            try {
                context.getRegistration().register(student, section, LocalDate.now());
                message.setText("Registered for "
                        + section.getCourse().getCourseCode() + " section "
                        + section.getSectionNo() + ".");
                refresh.run();
            } catch (RegistrationException ex) {
                // Show ALL failed rules, like the console app does.
                StringBuilder text = new StringBuilder("Registration failed:\n");
                for (String error : ex.getErrors()) {
                    text.append("• ").append(error).append("\n");
                }
                message.setText(text.toString());
            }
        });

        return new VBox(12,
                UiKit.sectionLabel("Semester"), semesterBox,
                UiKit.sectionLabel("Sections"), sectionsList,
                registerButton, message);
    }

    private VBox myCoursesPane() {
        ListView<Enrollment> enrollmentsList = new ListView<>();
        enrollmentsList.setPrefHeight(300);
        enrollmentsList.setCellFactory(list -> new javafx.scene.control.ListCell<Enrollment>() {
            @Override
            protected void updateItem(Enrollment enrollment, boolean empty) {
                super.updateItem(enrollment, empty);
                if (empty || enrollment == null) {
                    setText(null);
                } else {
                    CourseSection section = enrollment.getCourseSection();
                    String grade = enrollment.getGrade() == null
                            ? "" : "  •  Grade: " + enrollment.getGrade();
                    setText(section.getCourse().getCourseCode() + " — "
                            + section.getCourse().getCourseName()
                            + " (Section " + section.getSectionNo() + ")"
                            + "  •  " + enrollment.getStatus() + grade);
                }
            }
        });

        Label message = new Label();
        message.setWrapText(true);

        Runnable refresh = () -> enrollmentsList.setItems(FXCollections.observableArrayList(
                student.getAcademicRecord().getEnrollments()));
        refresh.run();

        Button withdrawButton = UiKit.primaryButton("Withdraw from Selected Enrollment");
        withdrawButton.setOnAction(e -> {
            Enrollment enrollment = enrollmentsList.getSelectionModel().getSelectedItem();
            if (enrollment == null) {
                message.setText("Select an enrollment first.");
                return;
            }
            if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
                message.setText("Only an active enrollment can be withdrawn.");
                return;
            }
            try {
                context.getRegistration().withdraw(student, enrollment, LocalDate.now());
                message.setText("Withdrawn from "
                        + enrollment.getCourseSection().getCourse().getCourseCode() + ".");
                refresh.run();
            } catch (RegistrationException ex) {
                StringBuilder text = new StringBuilder("Withdrawal failed:\n");
                for (String error : ex.getErrors()) {
                    text.append("• ").append(error).append("\n");
                }
                message.setText(text.toString());
            }
        });

        return new VBox(12,
                UiKit.sectionLabel("My enrollments"), enrollmentsList,
                withdrawButton, message);
    }

    private VBox transcriptPane() {
        Transcript transcript = context.getReports().transcript(student);
        ArrayList<String> lines = new ArrayList<>();
        for (SemesterSummary summary : transcript.getSemesters()) {
            lines.add(summary.getSemester() + "  •  GPA "
                    + UiKit.formatGpa(summary.getGpa())
                    + "  •  " + summary.getRegisteredCredits() + " credits");
            for (Enrollment enrollment : summary.getEnrollments()) {
                String grade = enrollment.getGrade() == null
                        ? enrollment.getStatus().toString()
                        : enrollment.getGrade().toString();
                lines.add("    " + enrollment.getCourseSection().getCourse().getCourseCode()
                        + " — " + enrollment.getCourseSection().getCourse().getCourseName()
                        + "  •  " + grade);
            }
        }
        lines.add("CGPA: " + UiKit.formatGpa(transcript.getCgpa())
                + "  •  Earned credits: " + transcript.getEarnedCredits());
        if (lines.isEmpty()) {
            lines.add("No enrollments yet.");
        }
        ListView<String> list = new ListView<>(FXCollections.observableArrayList(lines));
        list.setPrefHeight(420);
        return new VBox(12, UiKit.sectionLabel("Transcript"), list);
    }

    private VBox graduationPane() {
        GraduationReport report = context.getGraduation().checkGraduationEligibility(student);
        VBox card;
        if (report.isEligible()) {
            card = UiKit.card(new Label("You are eligible to graduate."),
                    new Label("CGPA: " + UiKit.formatGpa(
                            student.getAcademicRecord().calculateCGPA())
                            + " (minimum " + student.getProgram().getMinimumCGPA() + ")"),
                    new Label("Earned credits: "
                            + student.getAcademicRecord().getEarnedCredits()
                            + " / " + student.getProgram().getTotalCreditsRequired()));
        } else {
            ArrayList<String> items = new ArrayList<>();
            items.add("Not eligible yet:");
            items.addAll(report.getUnmetReasons());
            card = UiKit.card(new Label(String.join("\n• ", items)));
        }
        return new VBox(12, UiKit.sectionLabel("Graduation eligibility"), card);
    }

    private VBox notificationsPane() {
        ArrayList<String> lines = new ArrayList<>();
        for (Notification notification : context.getNotifications().getInbox(student)) {
            lines.add(notification.getSentAt() + " — " + notification.getMessage());
        }
        if (lines.isEmpty()) {
            lines.add("No notifications.");
        }
        ListView<String> list = new ListView<>(FXCollections.observableArrayList(lines));
        list.setPrefHeight(420);
        return new VBox(12, UiKit.sectionLabel("Notifications"), list);
    }

    private VBox settingsPane() {
        PasswordField oldField = new PasswordField();
        oldField.setPromptText("Current 4-digit password");
        PasswordField newField = new PasswordField();
        newField.setPromptText("New 4-digit password");
        Label message = new Label();
        message.setWrapText(true);

        Button changeButton = UiKit.primaryButton("Change Password");
        changeButton.setOnAction(e -> {
            try {
                student.changePassword(oldField.getText(), newField.getText());
                context.getStore().save();
                message.setText("Password updated.");
            } catch (IllegalArgumentException ex) {
                message.setText(ex.getMessage());
            }
        });

        Button signOut = UiKit.plainButton("Sign Out");
        signOut.setOnAction(ev -> app.signOut());

        VBox card = UiKit.card(oldField, newField, changeButton, message);
        return new VBox(12, UiKit.sectionLabel("Change password"), card, signOut);
    }
}
