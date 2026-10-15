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
 * Built with Google Material Design 3 (M3) components matching gym-management-system m3fx style.
 */
public class GraduationView extends VBox {

    private final UniversityDataStore store = UniversityDataStore.getInstance();

    private final ComboBox<Student> studentCombo = new ComboBox<>();
    private final Label lblEligibilityBadge = new Label("NO CANDIDATE SELECTED");
    private final Label lblProgramName = new Label("-");
    private final Label lblRequiredCredits = new Label("-");
    private final Label lblCreditsProgress = new Label("0 / 0 credits");
    private final ProgressBar creditsProgressBar = new ProgressBar(0.0);
    private final ListView<String> completedCoursesList = new ListView<>();
    private final ListView<String> unfulfilledReasonsList = new ListView<>();

    public GraduationView() {
        setSpacing(20);
        setPadding(new Insets(10, 0, 24, 0));

        initView();
        refresh();
        store.addChangeListener(this::refresh);
    }

    private void initView() {
        // Page header
        VBox titleBox = new VBox(4);
        Label title = new Label("Degree Audit & Graduation Checker");
        title.getStyleClass().add("m3-headline-medium");
        Label subtitle = new Label("Audit student degree progress against required curriculum, credit thresholds, and academic standing");
        subtitle.getStyleClass().add("m3-body-medium");
        titleBox.getChildren().addAll(title, subtitle);

        // Candidate Selector Card (M3 Elevated)
        HBox selectorCard = new HBox(16);
        selectorCard.getStyleClass().add("m3-card-elevated");
        selectorCard.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("🎓");
        iconLabel.setStyle("-fx-font-size: 24px;");

        VBox selectPromptBox = new VBox(2);
        Label lblSelectTitle = new Label("Candidate Degree Audit");
        lblSelectTitle.getStyleClass().add("m3-title-medium");
        Label lblSelectSub = new Label("Select an active student candidate to evaluate graduation criteria");
        lblSelectSub.getStyleClass().add("m3-body-medium");
        selectPromptBox.getChildren().addAll(lblSelectTitle, lblSelectSub);
        HBox.setHgrow(selectPromptBox, Priority.ALWAYS);

        studentCombo.setPrefWidth(360);
        studentCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Student s) { return s != null ? s.getStudentNumber() + " - " + s.getName() : ""; }
            @Override public Student fromString(String s) { return null; }
        });
        studentCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldV, selected) -> runGraduationAudit(selected));

        Button btnRefreshAudit = new Button("Re-evaluate");
        btnRefreshAudit.getStyleClass().addAll("m3-button", "m3-button-tonal");
        btnRefreshAudit.setOnAction(e -> runGraduationAudit(studentCombo.getValue()));

        selectorCard.getChildren().addAll(iconLabel, selectPromptBox, studentCombo, btnRefreshAudit);

        // Audit Summary Card (M3 Elevated)
        VBox summaryCard = new VBox(16);
        summaryCard.getStyleClass().add("m3-card-elevated");

        HBox statusRow = new HBox(24);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        lblEligibilityBadge.getStyleClass().addAll("m3-badge");
        lblEligibilityBadge.setStyle("-fx-font-size: 14px; -fx-padding: 8px 18px;");

        VBox programInfoBox = new VBox(2);
        Label lblProgTitle = new Label("Degree Program");
        lblProgTitle.getStyleClass().add("m3-body-medium");
        lblProgramName.getStyleClass().add("m3-title-medium");
        programInfoBox.getChildren().addAll(lblProgTitle, lblProgramName);
        HBox.setHgrow(programInfoBox, Priority.ALWAYS);

        VBox reqCreditsBox = new VBox(2);
        reqCreditsBox.setAlignment(Pos.CENTER_RIGHT);
        Label lblReqTitle = new Label("Degree Minimum Credits");
        lblReqTitle.getStyleClass().add("m3-body-medium");
        lblRequiredCredits.getStyleClass().add("m3-title-medium");
        reqCreditsBox.getChildren().addAll(lblReqTitle, lblRequiredCredits);

        statusRow.getChildren().addAll(lblEligibilityBadge, programInfoBox, reqCreditsBox);

        // Progress bar row
        VBox progressBox = new VBox(8);
        HBox progressLabels = new HBox();
        Label lblProgressTitle = new Label("Curriculum Credit Completion:");
        lblProgressTitle.getStyleClass().add("m3-body-medium");
        HBox.setHgrow(lblProgressTitle, Priority.ALWAYS);
        lblCreditsProgress.setStyle("-fx-font-weight: bold; -fx-text-fill: -md-sys-color-primary; -fx-font-size: 13px;");
        progressLabels.getChildren().addAll(lblProgressTitle, lblCreditsProgress);

        creditsProgressBar.setMaxWidth(Double.MAX_VALUE);
        creditsProgressBar.setPrefHeight(12);

        progressBox.getChildren().addAll(progressLabels, creditsProgressBar);

        summaryCard.getChildren().addAll(statusRow, new Separator(), progressBox);

        // Two columns: Completed Courses vs Unfulfilled Reasons
        HBox splitBox = new HBox(20);
        VBox.setVgrow(splitBox, Priority.ALWAYS);

        // Left: Completed Courses
        VBox completedCard = new VBox(12);
        completedCard.getStyleClass().add("m3-card-elevated");
        HBox.setHgrow(completedCard, Priority.ALWAYS);

        HBox completedHeader = new HBox(8);
        completedHeader.setAlignment(Pos.CENTER_LEFT);
        Label lblCompletedIcon = new Label("✅");
        Label lblCompletedTitle = new Label("Completed Core & Elective Courses");
        lblCompletedTitle.getStyleClass().add("m3-title-medium");
        completedHeader.getChildren().addAll(lblCompletedIcon, lblCompletedTitle);

        completedCoursesList.setPlaceholder(new Label("No completed courses recorded for this student"));
        VBox.setVgrow(completedCoursesList, Priority.ALWAYS);

        completedCard.getChildren().addAll(completedHeader, completedCoursesList);

        // Right: Unfulfilled Requirements
        VBox unfulfilledCard = new VBox(12);
        unfulfilledCard.getStyleClass().add("m3-card-elevated");
        HBox.setHgrow(unfulfilledCard, Priority.ALWAYS);

        HBox unfulfilledHeader = new HBox(8);
        unfulfilledHeader.setAlignment(Pos.CENTER_LEFT);
        Label lblUnfulfilledIcon = new Label("📋");
        Label lblUnfulfilledTitle = new Label("Audit Findings & Outstanding Deficiencies");
        lblUnfulfilledTitle.getStyleClass().add("m3-title-medium");
        unfulfilledHeader.getChildren().addAll(lblUnfulfilledIcon, lblUnfulfilledTitle);

        unfulfilledReasonsList.setPlaceholder(new Label("No outstanding deficiencies — Student cleared!"));
        VBox.setVgrow(unfulfilledReasonsList, Priority.ALWAYS);

        unfulfilledCard.getChildren().addAll(unfulfilledHeader, unfulfilledReasonsList);

        splitBox.getChildren().addAll(completedCard, unfulfilledCard);

        getChildren().addAll(titleBox, selectorCard, summaryCard, splitBox);
    }

    private void runGraduationAudit(Student student) {
        if (student == null) {
            lblEligibilityBadge.setText("NO CANDIDATE SELECTED");
            lblEligibilityBadge.getStyleClass().setAll("m3-badge");
            lblProgramName.setText("-");
            lblRequiredCredits.setText("-");
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
        lblRequiredCredits.setText(requiredCredits + " credits");

        double progress = requiredCredits > 0 ? (double) earnedCredits / requiredCredits : 0.0;
        int pct = (int) Math.round(progress * 100);
        lblCreditsProgress.setText(earnedCredits + " / " + requiredCredits + " credits earned (" + pct + "%)");
        creditsProgressBar.setProgress(Math.min(1.0, progress));

        GraduationReport report = store.getGraduationService().checkGraduationEligibility(student);

        lblEligibilityBadge.getStyleClass().setAll("m3-badge");
        if (report.isEligible()) {
            lblEligibilityBadge.setText("✓ GRADUATION ELIGIBLE");
            lblEligibilityBadge.getStyleClass().add("m3-badge-success");
        } else {
            lblEligibilityBadge.setText("⚠ REQUIREMENTS PENDING");
            lblEligibilityBadge.getStyleClass().add("m3-badge-warning");
        }

        // List completed courses
        completedCoursesList.getItems().clear();
        for (String code : report.getCompletedCourses()) {
            Course c = store.getCourses().stream()
                    .filter(course -> course.getCourseCode().equals(code))
                    .findFirst()
                    .orElse(null);
            String title = (c != null) ? c.getCourseName() + " (" + c.getCredits() + " cr)" : "";
            completedCoursesList.getItems().add("✓  " + code + " — " + title);
        }

        // List unfulfilled requirements
        unfulfilledReasonsList.getItems().clear();
        if (report.isEligible()) {
            unfulfilledReasonsList.getItems().add("✓ All required core curriculum courses completed.");
            unfulfilledReasonsList.getItems().add("✓ Minimum degree credit threshold satisfied (" + earnedCredits + "/" + requiredCredits + ").");
            unfulfilledReasonsList.getItems().add("✓ Student academic standing is in good health.");
            unfulfilledReasonsList.getItems().add("✓ Cleared for diploma conferral.");
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
