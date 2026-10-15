package com.university.registration.ui.views;

import com.university.registration.dto.GraduationReport;
import com.university.registration.model.Course;
import com.university.registration.model.Program;
import com.university.registration.model.Student;
import com.university.registration.ui.context.UniversityDataStore;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Degree Audit and Graduation Eligibility evaluation view.
 * Verifies required course completion, credit requirements, and unfulfilled criteria.
 */
public class GraduationView extends VBox {

    private final UniversityDataStore store = UniversityDataStore.getInstance();

    private final ComboBox<Student> studentCombo = new ComboBox<>();
    private final Label lblEligibilityBadge = new Label("-");
    private final Label lblProgramName = new Label("-");
    private final Label lblCreditsProgress = new Label("0 / 0 credits");
    private final ProgressBar creditsProgressBar = new ProgressBar(0.0);
    private final ListView<String> completedCoursesList = new ListView<>();
    private final ListView<String> unfulfilledReasonsList = new ListView<>();

    public GraduationView() {
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
        Label title = new Label("Degree Audit & Graduation Checker");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Audit student degree progress against required curriculum and credit thresholds");
        subtitle.getStyleClass().add("header-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // Selector Bar
        HBox selectorCard = new HBox(16);
        selectorCard.getStyleClass().add("card");
        selectorCard.setAlignment(Pos.CENTER_LEFT);

        Label lblSelect = new Label("Select Candidate Student:");
        lblSelect.getStyleClass().add("card-subtitle");

        studentCombo.setPrefWidth(340);
        studentCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Student s) { return s != null ? s.getStudentNumber() + " - " + s.getName() : ""; }
            @Override public Student fromString(String s) { return null; }
        });
        studentCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldV, selected) -> runGraduationAudit(selected));

        selectorCard.getChildren().addAll(lblSelect, studentCombo);

        // Audit Summary Card
        VBox summaryCard = new VBox(14);
        summaryCard.getStyleClass().add("card");

        HBox badgeRow = new HBox(20);
        badgeRow.setAlignment(Pos.CENTER_LEFT);

        lblEligibilityBadge.setStyle("-fx-font-size: 16px; -fx-padding: 6px 16px;");

        VBox programInfoBox = new VBox(4);
        Label lblProgTitle = new Label("Degree Program:");
        lblProgTitle.getStyleClass().add("card-subtitle");
        lblProgramName.setStyle("-fx-font-weight: bold; -fx-text-fill: #f8fafc;");
        programInfoBox.getChildren().addAll(lblProgTitle, lblProgramName);

        badgeRow.getChildren().addAll(lblEligibilityBadge, programInfoBox);

        // Progress bar row
        VBox progressBox = new VBox(6);
        HBox progressLabels = new HBox();
        Label lblProgessTitle = new Label("Curriculum Credit Completion:");
        lblProgessTitle.getStyleClass().add("card-subtitle");
        HBox.setHgrow(lblProgessTitle, Priority.ALWAYS);
        lblCreditsProgress.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8;");
        progressLabels.getChildren().addAll(lblProgessTitle, lblCreditsProgress);

        creditsProgressBar.setMaxWidth(Double.MAX_VALUE);
        creditsProgressBar.setPrefHeight(12);

        progressBox.getChildren().addAll(progressLabels, creditsProgressBar);

        summaryCard.getChildren().addAll(badgeRow, new Separator(), progressBox);

        // Two columns: Completed Courses vs Unfulfilled Reasons
        HBox splitBox = new HBox(20);
        VBox.setVgrow(splitBox, Priority.ALWAYS);

        // Left: Completed Courses
        VBox completedCard = new VBox(12);
        completedCard.getStyleClass().add("card");
        HBox.setHgrow(completedCard, Priority.ALWAYS);

        Label lblCompletedTitle = new Label("Completed Courses (Passed)");
        lblCompletedTitle.getStyleClass().add("card-title");
        completedCoursesList.setPlaceholder(new Label("No completed courses"));
        VBox.setVgrow(completedCoursesList, Priority.ALWAYS);

        completedCard.getChildren().addAll(lblCompletedTitle, completedCoursesList);

        // Right: Unfulfilled Requirements
        VBox unfulfilledCard = new VBox(12);
        unfulfilledCard.getStyleClass().add("card");
        HBox.setHgrow(unfulfilledCard, Priority.ALWAYS);

        Label lblUnfulfilledTitle = new Label("Unfulfilled Requirements & Deficiencies");
        lblUnfulfilledTitle.getStyleClass().add("card-title");
        unfulfilledReasonsList.setPlaceholder(new Label("No outstanding deficiencies"));
        VBox.setVgrow(unfulfilledReasonsList, Priority.ALWAYS);

        unfulfilledCard.getChildren().addAll(lblUnfulfilledTitle, unfulfilledReasonsList);

        splitBox.getChildren().addAll(completedCard, unfulfilledCard);

        getChildren().addAll(titleBox, selectorCard, summaryCard, splitBox);
    }

    private void runGraduationAudit(Student student) {
        if (student == null) {
            lblEligibilityBadge.setText("No Student Selected");
            lblEligibilityBadge.getStyleClass().clear();
            lblEligibilityBadge.getStyleClass().add("badge");
            lblProgramName.setText("-");
            lblCreditsProgress.setText("0 / 0 credits");
            creditsProgressBar.setProgress(0.0);
            completedCoursesList.getItems().clear();
            unfulfilledReasonsList.getItems().clear();
            return;
        }

        Program program = student.getProgram();
        lblProgramName.setText(program.getName());

        int earnedCredits = student.getAcademicRecord().getEarnedCredits();
        int requiredCredits = program.getTotalCreditsRequired();
        lblCreditsProgress.setText(earnedCredits + " / " + requiredCredits + " credits earned");
        double progress = requiredCredits > 0 ? (double) earnedCredits / requiredCredits : 0.0;
        creditsProgressBar.setProgress(Math.min(1.0, progress));

        GraduationReport report = store.getGraduationService().checkGraduationEligibility(student);

        lblEligibilityBadge.getStyleClass().clear();
        lblEligibilityBadge.getStyleClass().add("badge");
        if (report.isEligible()) {
            lblEligibilityBadge.setText("✓ GRADUATION ELIGIBLE");
            lblEligibilityBadge.getStyleClass().add("badge-success");
        } else {
            lblEligibilityBadge.setText("⚠ REQUIREMENTS PENDING");
            lblEligibilityBadge.getStyleClass().add("badge-warning");
        }

        // List completed courses
        completedCoursesList.getItems().clear();
        for (String code : report.getCompletedCourses()) {
            Course c = store.getCourses().stream()
                    .filter(course -> course.getCourseCode().equals(code))
                    .findFirst()
                    .orElse(null);
            String title = (c != null) ? c.getCourseName() + " (" + c.getCredits() + " cr)" : "";
            completedCoursesList.getItems().add("✓  " + code + " - " + title);
        }

        // List unfulfilled requirements
        unfulfilledReasonsList.getItems().clear();
        if (report.isEligible()) {
            unfulfilledReasonsList.getItems().add("✓ All required core courses completed.");
            unfulfilledReasonsList.getItems().add("✓ Minimum degree credit threshold satisfied.");
            unfulfilledReasonsList.getItems().add("✓ Student is cleared for graduation.");
        } else {
            for (String reason : report.getUnfulfilledReasons()) {
                unfulfilledReasonsList.getItems().add("✗  " + reason);
            }
        }
    }

    public void refresh() {
        Student prev = studentCombo.getValue();
        studentCombo.setItems(FXCollections.observableArrayList(store.getStudentRepository().findAll()));
        if (prev != null) {
            store.getStudentRepository().findById(prev.getUserId())
                    .ifPresent(studentCombo.getSelectionModel()::select);
        } else if (!studentCombo.getItems().isEmpty()) {
            studentCombo.getSelectionModel().selectFirst();
        }
    }
}
