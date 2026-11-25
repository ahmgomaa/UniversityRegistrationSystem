package ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
 * Student portal, built around one idea: show only what the student
 * needs right now, and fetch the rest when they ask for it.
 *
 * Home shows the essentials only — who they are, one line of progress
 * (CGPA, credits, unread messages), the courses they are in now, and
 * the single next action ("Register for courses"). Transcript,
 * Graduation, Notifications and Settings are not built at login at all:
 * each one is built fresh from the services every time it is opened,
 * so it can never go stale after a register, withdraw or grade.
 *
 * Every action calls the existing services — nothing is reimplemented
 * here — and after every change the affected screen is rebuilt and the
 * result is confirmed against the database (see SaveCheck).
 *
 * Controls, layouts and styling are the ones from Lecture 7/8:
 * BorderPane, VBox, Label, Button, ComboBox, ListView, TextField,
 * setOnAction handlers and inline CSS. No TabPane, no custom cells.
 */
public class StudentView {

    private final AppContext context;
    private final JavaFxApp app;
    private final Student student;

    private BorderPane root;
    private String current = "Home";
    private final java.util.Map<String, Button> navButtons =
            new java.util.LinkedHashMap<>();

    public StudentView(AppContext context, JavaFxApp app, Student student) {
        this.context = context;
        this.app = app;
        this.student = student;
    }

    /** Side navigation on the left, the open screen in the centre. */
    public BorderPane build() {
        root = new BorderPane();

        VBox nav = new VBox(6);
        nav.setPadding(new Insets(16, 10, 16, 16));
        nav.setPrefWidth(180);
        nav.getChildren().addAll(
                navItem("nav-home", "Home", this::showHome),
                navItem("nav-courses", "My Courses", this::showCourses),
                navItem("nav-register", "Register", this::showRegister),
                navItem("nav-transcript", "Transcript", this::showTranscript),
                navItem("nav-graduation", "Graduation", this::showGraduation),
                navItem("nav-notifications", "Notifications", this::showNotifications),
                navItem("nav-settings", "Settings", this::showSettings));
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        nav.getChildren().add(spacer);
        Button signOut = UiKit.plainButton("Sign Out");
        signOut.setId("nav-signout");
        signOut.setOnAction(e -> app.signOut());
        nav.getChildren().add(signOut);

        root.setLeft(nav);
        showHome();
        return root;
    }

    private Button navItem(String id, String text, Runnable action) {
        Button button = UiKit.navButton(text);
        button.setId(id);
        button.setOnAction(e -> action.run());
        navButtons.put(text, button);
        return button;
    }

    /** Highlight the open screen in the side navigation. */
    private void updateNavSelection() {
        for (java.util.Map.Entry<String, Button> entry : navButtons.entrySet()) {
            UiKit.setNavSelected(entry.getValue(), entry.getKey().equals(current));
        }
    }

    /** Rebuild the screen that is open now, from fresh backend state. */
    private void refreshCurrent() {
        switch (current) {
            case "My Courses":
                showCourses();
                break;
            case "Register":
                showRegister();
                break;
            case "Transcript":
                showTranscript();
                break;
            case "Graduation":
                showGraduation();
                break;
            case "Notifications":
                showNotifications();
                break;
            case "Settings":
                showSettings();
                break;
            default:
                showHome();
                break;
        }
    }

    private VBox screen(String title, javafx.scene.Node... children) {
        VBox box = new VBox(12, UiKit.title(title));
        box.getChildren().addAll(children);
        box.setPadding(new Insets(20));
        return box;
    }

    // ---------------------------------------------------------------- Home

    /** Only the essentials: identity, one line of progress, courses now,
     *  and the single next action. Everything else waits in the nav. */
    private void showHome() {
        current = "Home";
        updateNavSelection();

        int unread = context.getNotifications().getInbox(student).size();
        double cgpa = student.getAcademicRecord().calculateCGPA();
        int earned = student.getAcademicRecord().getEarnedCredits();
        int required = student.getProgram().getTotalCreditsRequired();

        Label identity = new Label(student.getName() + "  •  " + student.getUserId()
                + "  •  " + student.getProgram().getName());
        identity.setFont(javafx.scene.text.Font.font("System",
                javafx.scene.text.FontWeight.BOLD, 14));
        Label standing = UiKit.caption("Academic status: "
                + student.getAcademicStatus());
        VBox who = UiKit.card(identity, standing);

        VBox cgpaCard = UiKit.statCard(UiKit.formatGpa(cgpa), "CGPA", UiKit.ACCENT);
        VBox creditsCard = UiKit.statCard(earned + " / " + required,
                "Credits earned", UiKit.GREEN);
        creditsCard.getChildren().add(UiKit.progressBar(
                required == 0 ? 0 : (double) earned / required, UiKit.GREEN));
        VBox unreadCard = UiKit.statCard(String.valueOf(unread),
                "Notifications", unread > 0 ? UiKit.ORANGE : UiKit.GRAY);

        ArrayList<String> active = activeEnrollmentLines();
        ListView<String> coursesList = new ListView<>(
                FXCollections.observableArrayList(active));
        coursesList.setPrefHeight(220);
        if (active.isEmpty()) {
            coursesList.setItems(FXCollections.observableArrayList(
                    "You are not registered in any course yet."));
        }

        Button registerButton = UiKit.primaryButton("Register for courses");
        registerButton.setId("home-register-button");
        registerButton.setOnAction(e -> showRegister());

        javafx.scene.layout.HBox statsRow =
                UiKit.statRow(cgpaCard, creditsCard, unreadCard);
        root.setCenter(screen("Welcome, " + student.getName(),
                who,
                statsRow,
                UiKit.sectionLabel("My courses now"),
                coursesList,
                registerButton));
    }

    private ArrayList<String> activeEnrollmentLines() {
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

    // ------------------------------------------------------------ My Courses

    /** Every enrollment, newest state, with withdraw for the active one.
     *  The list holds plain text lines (Lecture 7: ListView of strings);
     *  the matching Enrollment objects are kept alongside for the action. */
    private void showCourses() {
        current = "My Courses";
        updateNavSelection();

        ArrayList<Enrollment> enrollments = student.getAcademicRecord().getEnrollments();
        showCoursesWithMessage(null, false);
    }

    /** My Courses, optionally showing the result of the action that
     *  just happened — the banner is added after the rebuild so a
     *  success message is never wiped by it. */
    private void showCoursesWithMessage(String message, boolean ok) {
        ArrayList<Enrollment> enrollments = student.getAcademicRecord().getEnrollments();
        ArrayList<String> lines = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            CourseSection section = enrollment.getCourseSection();
            String grade = enrollment.getGrade() == null
                    ? "" : "  •  Grade: " + enrollment.getGrade();
            lines.add(section.getCourse().getCourseCode() + " — "
                    + section.getCourse().getCourseName()
                    + " (Section " + section.getSectionNo() + ")"
                    + "  •  " + enrollment.getStatus() + grade);
        }
        ListView<String> enrollmentsList = new ListView<>(
                FXCollections.observableArrayList(lines));
        enrollmentsList.setPrefHeight(300);
        enrollmentsList.setId("courses-list");
        Label status = UiKit.statusLabel();
        if (message != null) {
            UiKit.showStatus(status, ok, message);
        }

        Button withdrawButton = UiKit.primaryButton("Withdraw Selected Course");
        withdrawButton.setId("courses-withdraw-button");
        withdrawButton.setOnAction(e -> {
            int index = enrollmentsList.getSelectionModel().getSelectedIndex();
            if (index < 0 || index >= enrollments.size()) {
                UiKit.showStatus(status, false, "Select a course first.");
                return;
            }
            Enrollment enrollment = enrollments.get(index);
            if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
                UiKit.showStatus(status, false,
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
                    // Rebuild first, then show the banner on the new screen.
                    showCoursesWithMessage(
                            "Withdrawn from " + code + ". Your seat has been released.",
                            true);
                } else {
                    showCoursesWithMessage(
                            "The change was not saved. Try again — if it keeps "
                                    + "failing, restart the app or contact the registrar.",
                            false);
                }
            } catch (RegistrationException ex) {
                UiKit.showStatus(status, false, ruleText("Withdrawal failed", ex));
            }
        });

        VBox content = screen("My courses",
                enrollmentsList, withdrawButton, status);
        if (enrollments.isEmpty()) {
            content.getChildren().setAll(UiKit.title("My courses"),
                    UiKit.emptyState("No enrollments yet. "
                            + "Open Register to pick your first course."));
        }
        root.setCenter(content);
    }

    // -------------------------------------------------------------- Register
    //
    // Step by step, one choice at a time:
    //   1. choose a semester -> 2. choose a section -> 3. confirm.
    // Rule feedback appears only at step 3, and only the rules the
    // chosen section actually failed — never a list of all 7 rules.

    private int regStep = 1;
    private Semester regSemester;
    private CourseSection regSection;

    /** Open the register flow from the start. */
    private void showRegister() {
        current = "Register";
        updateNavSelection();
        regStep = 1;
        regSemester = null;
        regSection = null;
        renderRegisterStep();
    }

    private void renderRegisterStep() {
        switch (regStep) {
            case 2:
                renderRegisterStep2();
                break;
            case 3:
                renderRegisterStep3();
                break;
            default:
                renderRegisterStep1();
                break;
        }
    }

    /** Three pills showing which registration step we are on. */
    private javafx.scene.layout.HBox stepIndicator(int step) {
        javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(8);
        String[] labels = {"1. Semester", "2. Section", "3. Confirm"};
        for (int i = 0; i < labels.length; i++) {
            box.getChildren().add(i + 1 == step
                    ? UiKit.pill(labels[i], UiKit.ACCENT)
                    : UiKit.softPill(labels[i], UiKit.GRAY));
        }
        return box;
    }

    private Button backHomeButton() {
        Button back = UiKit.plainButton("Back to Home");
        back.setId("register-home-button");
        back.setOnAction(e -> showHome());
        return back;
    }

    /** Step 1 of 3: choose a semester. */
    private void renderRegisterStep1() {
        ArrayList<Semester> semesters = context.getStore().getSemesters();
        ComboBox<Semester> semesterBox = new ComboBox<>(
                FXCollections.observableArrayList(semesters));
        semesterBox.setId("register-semester-box");
        semesterBox.setMaxWidth(Double.MAX_VALUE);
        if (regSemester != null) {
            semesterBox.setValue(regSemester);
        } else if (!semesters.isEmpty()) {
            semesterBox.setValue(semesters.get(0));
        }
        Label status = UiKit.statusLabel();

        Button continueButton = UiKit.primaryButton("Continue");
        continueButton.setId("register-continue-button");
        continueButton.setOnAction(e -> {
            regSemester = semesterBox.getValue();
            if (regSemester == null) {
                UiKit.showStatus(status, false, "Choose a semester first.");
                return;
            }
            regStep = 2;
            renderRegisterStep();
        });

        root.setCenter(screen("Register — Step 1 of 3",
                stepIndicator(1),
                UiKit.sectionLabel("Choose a semester"), semesterBox,
                continueButton, status, backHomeButton()));
    }

    /** Step 2 of 3: choose a section in that semester. */
    private void renderRegisterStep2() {
        ArrayList<CourseSection> sections =
                context.getStore().getSectionsInSemester(regSemester);
        ArrayList<String> lines = new ArrayList<>();
        for (CourseSection section : sections) {
            lines.add(BrowseView.describeWithSeats(context, section));
        }
        if (lines.isEmpty()) {
            lines.add("No sections in this semester.");
        }
        ListView<String> sectionsList = new ListView<>(
                FXCollections.observableArrayList(lines));
        sectionsList.setPrefHeight(300);
        sectionsList.setId("register-sections-list");
        Label status = UiKit.statusLabel();

        Button backButton = UiKit.plainButton("Back");
        backButton.setId("register-back-button");
        backButton.setOnAction(e -> {
            regStep = 1;
            renderRegisterStep();
        });
        Button continueButton = UiKit.primaryButton("Continue");
        continueButton.setId("register-continue-button");
        continueButton.setOnAction(e -> {
            int index = sectionsList.getSelectionModel().getSelectedIndex();
            if (index < 0 || index >= sections.size()) {
                UiKit.showStatus(status, false, "Select a section first.");
                return;
            }
            regSection = sections.get(index);
            regStep = 3;
            renderRegisterStep();
        });

        root.setCenter(screen("Register — Step 2 of 3",
                stepIndicator(2),
                UiKit.sectionLabel("Sections in " + regSemester), sectionsList,
                continueButton, backButton, status, backHomeButton()));
    }

    /** Step 3 of 3: confirm the one chosen section. */
    private void renderRegisterStep3() {
        Label status = UiKit.statusLabel();
        StringBuilder meetings = new StringBuilder();
        for (model.MeetingTime meeting : regSection.getMeetings()) {
            if (meetings.length() > 0) {
                meetings.append("  •  ");
            }
            meetings.append(meeting);
        }
        int seatsLeft = BrowseView.seatsLeft(context, regSection);
        VBox detail = UiKit.card(
                new Label(regSection.getCourse().getCourseCode() + " — "
                        + regSection.getCourse().getCourseName()),
                new Label("Section " + regSection.getSectionNo()
                        + "  •  " + regSection.getCourse().getCreditHours() + " credits"
                        + "  •  " + regSemester),
                new Label("Instructor: " + regSection.getInstructor().getName()),
                new Label("Schedule: " + (meetings.length() == 0
                        ? "to be announced" : meetings.toString())),
                new Label(seatsLeft <= 0 ? "This section is full."
                        : seatsLeft + " seats left."));

        Button backButton = UiKit.plainButton("Choose another section");
        backButton.setId("register-back-button");
        backButton.setOnAction(e -> {
            regStep = 2;
            renderRegisterStep();
        });
        Button confirmButton = UiKit.primaryButton("Confirm registration");
        confirmButton.setId("register-confirm-button");
        confirmButton.setOnAction(e -> {
            try {
                context.getRegistration().register(
                        student, regSection, LocalDate.now());
                String code = regSection.getCourse().getCourseCode();
                if (SaveCheck.enrollmentSaved(student.getUserId(),
                        regSection.getSectionId(), "ENROLLED")) {
                    UiKit.showStatus(status, true,
                            "Registered for " + code + " section "
                                    + regSection.getSectionNo() + ".");
                } else {
                    UiKit.showStatus(status, false,
                            "The change was not saved. Try again — if it keeps "
                                    + "failing, restart the app or contact the registrar.");
                }
                // The choice is done: offer home (fresh, shows the new
                // course) or another registration.
                Button againButton = UiKit.primaryButton("Register another course");
                againButton.setId("register-again-button");
                againButton.setOnAction(ev -> showRegister());
                root.setCenter(screen("Register — Done",
                        detail, status, againButton, backHomeButton()));
            } catch (RegistrationException ex) {
                // Only the rules this section failed, at the moment they fail.
                UiKit.showStatus(status, false, ruleText("Registration failed", ex));
            }
        });

        root.setCenter(screen("Register — Step 3 of 3",
                stepIndicator(3),
                UiKit.sectionLabel("Confirm registration"),
                detail, confirmButton, backButton, status, backHomeButton()));
    }

    // ------------------------------------------------------------- Transcript

    /** Built fresh from ReportService every time it is opened. */
    private void showTranscript() {
        current = "Transcript";
        updateNavSelection();

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
        VBox cgpaCard = UiKit.statCard(UiKit.formatGpa(transcript.getCgpa()),
                "CGPA", UiKit.ACCENT);
        VBox earnedCard = UiKit.statCard(String.valueOf(transcript.getEarnedCredits()),
                "Earned credits", UiKit.GREEN);
        root.setCenter(screen("Transcript",
                UiKit.statRow(cgpaCard, earnedCard), list));
    }

    // ------------------------------------------------------------- Graduation

    /** Built fresh from GraduationService every time it is opened. */
    private void showGraduation() {
        current = "Graduation";
        updateNavSelection();

        GraduationReport report = context.getGraduation().checkGraduationEligibility(student);
        int earnedCredits = student.getAcademicRecord().getEarnedCredits();
        int requiredCredits = student.getProgram().getTotalCreditsRequired();
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
        VBox progressCard = UiKit.card(
                UiKit.sectionLabel("Credit progress"),
                new Label(earnedCredits + " of " + requiredCredits + " credits"),
                UiKit.progressBar(requiredCredits == 0 ? 0
                        : (double) earnedCredits / requiredCredits,
                        report.isEligible() ? UiKit.GREEN : UiKit.ORANGE));
        root.setCenter(screen("Graduation eligibility", card, progressCard));
    }

    // ----------------------------------------------------------- Notifications

    /** Built fresh from the notification service every time it is opened. */
    private void showNotifications() {
        current = "Notifications";
        updateNavSelection();

        ArrayList<String> lines = new ArrayList<>();
        for (Notification notification : context.getNotifications().getInbox(student)) {
            lines.add(notification.getSentAt() + " — " + notification.getMessage());
        }
        if (lines.isEmpty()) {
            lines.add("No notifications.");
        }
        ListView<String> list = new ListView<>(FXCollections.observableArrayList(lines));
        list.setPrefHeight(420);
        root.setCenter(screen("Notifications", list));
    }

    // --------------------------------------------------------------- Settings

    private void showSettings() {
        current = "Settings";
        updateNavSelection();

        PasswordField oldField = new PasswordField();
        oldField.setPromptText("Current 4-digit password");
        oldField.setId("settings-old-password");
        PasswordField newField = new PasswordField();
        newField.setPromptText("New 4-digit password");
        newField.setId("settings-new-password");
        Label status = UiKit.statusLabel();

        Button changeButton = UiKit.primaryButton("Change Password");
        changeButton.setId("settings-change-button");
        changeButton.setOnAction(e -> {
            try {
                student.changePassword(oldField.getText(), newField.getText());
                context.getStore().save();
                if (SaveCheck.passwordSaved(student.getUserId(),
                        student.getPasswordHash())) {
                    UiKit.showStatus(status, true, "Password updated.");
                } else {
                    UiKit.showStatus(status, false,
                            "The change was not saved. Try again — if it keeps "
                                    + "failing, restart the app or contact the registrar.");
                }
            } catch (IllegalArgumentException ex) {
                UiKit.showStatus(status, false, ex.getMessage());
            }
        });

        Button signOut = UiKit.plainButton("Sign Out");
        signOut.setOnAction(ev -> app.signOut());

        VBox card = UiKit.card(oldField, newField, changeButton, status);
        root.setCenter(screen("Change password", card, signOut));
    }

    /** The failed rules from a RegistrationException, one per line. */
    private static String ruleText(String heading, RegistrationException ex) {
        StringBuilder text = new StringBuilder(heading + ":\n");
        for (String error : ex.getErrors()) {
            text.append("• ").append(error).append("\n");
        }
        return text.toString();
    }
}
