package com.university.registration.ui.views;

import com.university.registration.exception.RegistrationException;
import com.university.registration.model.*;
import com.university.registration.model.enums.EnrollmentStatus;
import com.university.registration.ui.context.UniversityDataStore;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Course Registration Desk view.
 * Allows interactive enrollment and withdrawal, checks prerequisites and capacity,
 * and enforces academic standing and semester deadlines.
 */
public class RegistrationView extends VBox {

    private final UniversityDataStore store = UniversityDataStore.getInstance();

    private final ComboBox<Student> studentCombo = new ComboBox<>();
    private final ComboBox<CourseSection> sectionCombo = new ComboBox<>();
    private final Button btnRegister = new Button("Register Course");
    private final TableView<Enrollment> enrollmentsTable = new TableView<>();
    private final Label alertBanner = new Label();

    // Pre-flight validation info labels
    private final Label lblStudentStanding = new Label("-");
    private final Label lblCoursePrereqs = new Label("-");
    private final Label lblSectionSeats = new Label("-");
    private final Label lblRegPeriodStatus = new Label("-");
    private final Button btnWithdraw = new Button("Withdraw Selected Course");

    public RegistrationView() {
        setSpacing(20);
        setPadding(new Insets(24));
        getStyleClass().add("content-area");

        initView();
        refresh();
        store.addChangeListener(this::refresh);
    }

    private void initView() {
        // Page header
        VBox titleBox = new VBox(4);
        Label title = new Label("Course Registration Desk");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Enroll students into sections, enforce prerequisites, capacity, and deadlines");
        subtitle.getStyleClass().add("header-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // Feedback banner
        alertBanner.setVisible(false);
        alertBanner.setManaged(false);
        alertBanner.setMaxWidth(Double.MAX_VALUE);
        alertBanner.setWrapText(true);

        // Top control card: Selection and Pre-flight checks
        VBox controlCard = new VBox(16);
        controlCard.getStyleClass().add("card");

        HBox selectorsRow = new HBox(16);
        selectorsRow.setAlignment(Pos.CENTER_LEFT);

        // Student selector
        VBox studentBox = new VBox(6);
        Label lblSelectStudent = new Label("Select Student:");
        lblSelectStudent.getStyleClass().add("card-subtitle");
        studentCombo.setPrefWidth(280);
        studentCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Student s) { return s != null ? s.getStudentNumber() + " - " + s.getName() : ""; }
            @Override public Student fromString(String s) { return null; }
        });
        studentCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> updatePreflightChecks());
        studentBox.getChildren().addAll(lblSelectStudent, studentCombo);

        // Section selector
        VBox sectionBox = new VBox(6);
        Label lblSelectSection = new Label("Select Course Section:");
        lblSelectSection.getStyleClass().add("card-subtitle");
        sectionCombo.setPrefWidth(380);
        sectionCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(CourseSection s) {
                return s != null ? s.getSectionId() + " (" + s.getCourse().getCourseCode() + " - " + s.getCourse().getCourseName() + ")" : "";
            }
            @Override public CourseSection fromString(String s) { return null; }
        });
        sectionCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> updatePreflightChecks());
        sectionBox.getChildren().addAll(lblSelectSection, sectionCombo);

        // Action button
        VBox actionBox = new VBox(6);
        actionBox.setAlignment(Pos.BOTTOM_LEFT);
        btnRegister.getStyleClass().add("btn-primary");
        btnRegister.setPrefHeight(36);
        btnRegister.setOnAction(e -> handleRegister());
        actionBox.getChildren().addAll(new Label(""), btnRegister);

        selectorsRow.getChildren().addAll(studentBox, sectionBox, actionBox);

        // Preflight validation display grid
        GridPane preflightGrid = new GridPane();
        preflightGrid.setHgap(24);
        preflightGrid.setVgap(8);
        preflightGrid.setPadding(new Insets(10, 0, 0, 0));

        Label lblStandingTitle = new Label("Student Status:");
        lblStandingTitle.getStyleClass().add("card-subtitle");
        Label lblPrereqTitle = new Label("Prerequisites:");
        lblPrereqTitle.getStyleClass().add("card-subtitle");
        Label lblSeatsTitle = new Label("Seat Availability:");
        lblSeatsTitle.getStyleClass().add("card-subtitle");
        Label lblRegPeriodTitle = new Label("Registration Window:");
        lblRegPeriodTitle.getStyleClass().add("card-subtitle");

        preflightGrid.add(lblStandingTitle, 0, 0);
        preflightGrid.add(lblStudentStanding, 1, 0);
        preflightGrid.add(lblPrereqTitle, 0, 1);
        preflightGrid.add(lblCoursePrereqs, 1, 1);
        preflightGrid.add(lblSeatsTitle, 2, 0);
        preflightGrid.add(lblSectionSeats, 3, 0);
        preflightGrid.add(lblRegPeriodTitle, 2, 1);
        preflightGrid.add(lblRegPeriodStatus, 3, 1);

        controlCard.getChildren().addAll(selectorsRow, new Separator(), preflightGrid);

        // Student's Current Enrollments Section
        VBox enrollmentsCard = new VBox(12);
        enrollmentsCard.getStyleClass().add("card");
        VBox.setVgrow(enrollmentsCard, Priority.ALWAYS);

        HBox enrollmentsHeader = new HBox(16);
        enrollmentsHeader.setAlignment(Pos.CENTER_LEFT);

        Label lblEnrollmentsTitle = new Label("Student's Active & Historical Enrollments");
        lblEnrollmentsTitle.getStyleClass().add("card-title");
        HBox.setHgrow(lblEnrollmentsTitle, Priority.ALWAYS);

        btnWithdraw.getStyleClass().add("btn-danger");
        btnWithdraw.setDisable(true);
        btnWithdraw.setOnAction(e -> handleWithdraw());

        enrollmentsHeader.getChildren().addAll(lblEnrollmentsTitle, btnWithdraw);

        setupEnrollmentsTable();

        enrollmentsCard.getChildren().addAll(enrollmentsHeader, enrollmentsTable);

        getChildren().addAll(titleBox, alertBanner, controlCard, enrollmentsCard);
    }

    private void setupEnrollmentsTable() {
        TableColumn<Enrollment, String> colSec = new TableColumn<>("SECTION");
        colSec.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSection().getSectionId()));
        colSec.setPrefWidth(140);

        TableColumn<Enrollment, String> colCourse = new TableColumn<>("COURSE");
        colCourse.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getSection().getCourse().getCourseCode() + " - "
                        + data.getValue().getSection().getCourse().getCourseName()));
        colCourse.setPrefWidth(260);

        TableColumn<Enrollment, String> colTerm = new TableColumn<>("SEMESTER");
        colTerm.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getSection().getSemester().toString()));
        colTerm.setPrefWidth(120);

        TableColumn<Enrollment, String> colStatus = new TableColumn<>("STATUS");
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        colStatus.setPrefWidth(120);
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    if ("ENROLLED".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-active");
                    } else if ("COMPLETED".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-info");
                    } else {
                        badge.getStyleClass().add("badge-danger");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TableColumn<Enrollment, String> colGrade = new TableColumn<>("GRADE");
        colGrade.setCellValueFactory(data -> {
            Grade g = data.getValue().getGrade();
            return new SimpleStringProperty(g != null ? g.getLetterGrade() + " (" + g.getScore() + ")" : "In Progress");
        });
        colGrade.setPrefWidth(130);

        enrollmentsTable.getColumns().addAll(colSec, colCourse, colTerm, colStatus, colGrade);

        enrollmentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, selected) -> {
            btnWithdraw.setDisable(selected == null || !selected.isActive());
        });
    }

    private void updatePreflightChecks() {
        Student student = studentCombo.getValue();
        CourseSection section = sectionCombo.getValue();
        LocalDate today = store.getSimulatedDate();

        if (student != null) {
            boolean canReg = student.getAcademicStatus().canRegister();
            lblStudentStanding.setText(student.getAcademicStatus().name() + (canReg ? " (Eligible)" : " (Restricted)"));
            lblStudentStanding.setStyle(canReg ? "-fx-text-fill: #34d399;" : "-fx-text-fill: #f87171;");
            loadStudentEnrollments(student);
        } else {
            lblStudentStanding.setText("-");
            enrollmentsTable.getItems().clear();
        }

        if (section != null) {
            lblSectionSeats.setText(section.getAvailableSeatsCount() + " of " + section.getCapacity() + " available");
            lblSectionSeats.setStyle(section.hasAvailableSeats() ? "-fx-text-fill: #34d399;" : "-fx-text-fill: #f87171;");

            Semester sem = section.getSemester();
            boolean open = sem.isInRegistrationPeriod(today);
            lblRegPeriodStatus.setText((open ? "OPEN" : "CLOSED") + " ("
                    + sem.getRegistrationPeriod().getStartDate().format(DateTimeFormatter.ofPattern("MMM dd")) + " - "
                    + sem.getRegistrationPeriod().getEndDate().format(DateTimeFormatter.ofPattern("MMM dd")) + ")");
            lblRegPeriodStatus.setStyle(open ? "-fx-text-fill: #34d399;" : "-fx-text-fill: #f87171;");

            if (student != null) {
                Course course = section.getCourse();
                if (course.getPrerequisites().isEmpty()) {
                    lblCoursePrereqs.setText("None required ✓");
                    lblCoursePrereqs.setStyle("-fx-text-fill: #34d399;");
                } else {
                    boolean allMet = true;
                    StringBuilder sb = new StringBuilder();
                    for (Course req : course.getPrerequisites()) {
                        boolean completed = student.getAcademicRecord().hasCompletedCourse(req);
                        if (!completed) allMet = false;
                        sb.append(req.getCourseCode()).append(completed ? " ✓ " : " ✗ ");
                    }
                    lblCoursePrereqs.setText(sb.toString().trim());
                    lblCoursePrereqs.setStyle(allMet ? "-fx-text-fill: #34d399;" : "-fx-text-fill: #f87171;");
                }
            } else {
                lblCoursePrereqs.setText("-");
            }
        } else {
            lblSectionSeats.setText("-");
            lblRegPeriodStatus.setText("-");
            lblCoursePrereqs.setText("-");
        }
    }

    private void handleRegister() {
        Student student = studentCombo.getValue();
        CourseSection section = sectionCombo.getValue();
        LocalDate today = store.getSimulatedDate();

        if (student == null || section == null) {
            showAlert("Please select both a student and a course section.", true);
            return;
        }

        try {
            Enrollment enrollment = store.getRegistrationService().register(student, section, today);
            showAlert("Registration Successful! Enrolled " + student.getName() + " in "
                    + section.getCourse().getCourseCode() + " (" + section.getSectionId() + ")", false);
            store.notifyStateChanged();
        } catch (RegistrationException ex) {
            showAlert("Registration Failed: " + ex.getMessage(), true);
        }
    }

    private void handleWithdraw() {
        Student student = studentCombo.getValue();
        Enrollment selectedEnrollment = enrollmentsTable.getSelectionModel().getSelectedItem();
        LocalDate today = store.getSimulatedDate();

        if (student == null || selectedEnrollment == null) {
            showAlert("Please select an enrollment to withdraw from.", true);
            return;
        }

        try {
            store.getRegistrationService().withdraw(student, selectedEnrollment, today);
            showAlert("Withdrawn successfully from " + selectedEnrollment.getSection().getCourse().getCourseCode(), false);
            store.notifyStateChanged();
        } catch (RegistrationException ex) {
            showAlert("Withdrawal Failed: " + ex.getMessage(), true);
        }
    }

    private void showAlert(String message, boolean isError) {
        alertBanner.setText(message);
        alertBanner.getStyleClass().removeAll("alert-box", "alert-success", "alert-error");
        alertBanner.getStyleClass().addAll("alert-box", isError ? "alert-error" : "alert-success");
        alertBanner.setVisible(true);
        alertBanner.setManaged(true);
    }

    private void loadStudentEnrollments(Student student) {
        if (student == null) {
            enrollmentsTable.getItems().clear();
            return;
        }
        enrollmentsTable.setItems(FXCollections.observableArrayList(
                student.getAcademicRecord().getEnrollments()
        ));
    }

    public void refresh() {
        Student prevStudent = studentCombo.getValue();
        CourseSection prevSection = sectionCombo.getValue();

        studentCombo.setItems(FXCollections.observableArrayList(store.getStudentRepository().findAll()));
        sectionCombo.setItems(FXCollections.observableArrayList(store.getCourseSections()));

        if (prevStudent != null) {
            store.getStudentRepository().findById(prevStudent.getUserId())
                    .ifPresent(studentCombo.getSelectionModel()::select);
        } else if (!studentCombo.getItems().isEmpty()) {
            studentCombo.getSelectionModel().selectFirst();
        }

        if (prevSection != null) {
            store.getCourseSections().stream()
                    .filter(s -> s.getSectionId().equals(prevSection.getSectionId()))
                    .findFirst()
                    .ifPresent(sectionCombo.getSelectionModel()::select);
        } else if (!sectionCombo.getItems().isEmpty()) {
            sectionCombo.getSelectionModel().selectFirst();
        }

        updatePreflightChecks();
    }
}
