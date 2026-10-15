package com.university.registration.ui.views;

import com.university.registration.dto.Notification;
import com.university.registration.model.Semester;
import com.university.registration.model.Student;
import com.university.registration.ui.context.UniversityDataStore;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Overview dashboard with Material Design 3 cards, metrics, and notification feed.
 */
public class DashboardView extends VBox {

    private final UniversityDataStore store = UniversityDataStore.getInstance();
    private final Label lblStudentsCount = new Label("0");
    private final Label lblCoursesCount = new Label("0");
    private final Label lblSectionsCount = new Label("0");
    private final Label lblCreditsCount = new Label("0");
    private final Label lblCurrentTerm = new Label("-");
    private final Label lblRegStatus = new Label("-");
    private final Label lblCurrentDate = new Label("-");
    private final ListView<String> recentActivityList = new ListView<>();

    public DashboardView() {
        setSpacing(24);
        setPadding(new Insets(10, 0, 24, 0));

        initView();
        refresh();
        store.addChangeListener(this::refresh);
    }

    private void initView() {
        // Page header
        VBox titleBox = new VBox(4);
        Label title = new Label("Executive Dashboard");
        title.getStyleClass().add("m3-headline-medium");
        Label subtitle = new Label("System metrics, active term timeline, and recent campus activity");
        subtitle.getStyleClass().add("m3-body-medium");
        titleBox.getChildren().addAll(title, subtitle);

        // Stats grid
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(16);
        statsGrid.setVgap(16);

        VBox cardStudents = createStatCard("Total Enrolled Students", lblStudentsCount, "👥");
        VBox cardCourses = createStatCard("Course Catalog Offerings", lblCoursesCount, "📚");
        VBox cardSections = createStatCard("Scheduled Course Sections", lblSectionsCount, "🗓️");
        VBox cardCredits = createStatCard("Completed Quality Credits", lblCreditsCount, "🎖️");

        statsGrid.add(cardStudents, 0, 0);
        statsGrid.add(cardCourses, 1, 0);
        statsGrid.add(cardSections, 2, 0);
        statsGrid.add(cardCredits, 3, 0);

        for (int i = 0; i < 4; i++) {
            GridPane.setHgrow(statsGrid.getChildren().get(i), Priority.ALWAYS);
        }

        // Two-column layout: Academic Session Info & Recent Activity
        HBox bottomSection = new HBox(20);
        bottomSection.setAlignment(Pos.TOP_LEFT);

        // Academic Session Card (M3 Elevated Card)
        VBox sessionCard = new VBox(16);
        sessionCard.getStyleClass().add("m3-card-elevated");
        sessionCard.setPrefWidth(420);

        Label sessionHeader = new Label("Academic Term Overview");
        sessionHeader.getStyleClass().add("m3-title-medium");

        GridPane sessionDetails = new GridPane();
        sessionDetails.setHgap(16);
        sessionDetails.setVgap(12);

        Label lbl1 = new Label("Current System Date:");
        lbl1.getStyleClass().add("m3-body-medium");
        Label lbl2 = new Label("Active Academic Term:");
        lbl2.getStyleClass().add("m3-body-medium");
        Label lbl3 = new Label("Registration Status:");
        lbl3.getStyleClass().add("m3-body-medium");

        lblCurrentDate.setStyle("-fx-font-weight: bold; -fx-text-fill: -md-sys-color-primary;");
        lblCurrentTerm.setStyle("-fx-font-weight: bold; -fx-text-fill: -md-sys-color-on-surface;");

        sessionDetails.add(lbl1, 0, 0);
        sessionDetails.add(lblCurrentDate, 1, 0);
        sessionDetails.add(lbl2, 0, 1);
        sessionDetails.add(lblCurrentTerm, 1, 1);
        sessionDetails.add(lbl3, 0, 2);
        sessionDetails.add(lblRegStatus, 1, 2);

        Label sessionNote = new Label(
                "💡 Material Design 3 Tip: Switch themes (🌙/☀️) or choose a seed palette (🎨) from the Top App Bar to customize your experience.");
        sessionNote.setWrapText(true);
        sessionNote.setStyle("-fx-font-size: 11px; -fx-text-fill: -md-sys-color-on-surface-variant;");

        sessionCard.getChildren().addAll(sessionHeader, new Separator(), sessionDetails, sessionNote);

        // Recent System Activity Card (M3 Outlined Card)
        VBox activityCard = new VBox(12);
        activityCard.getStyleClass().add("m3-card-outlined");
        HBox.setHgrow(activityCard, Priority.ALWAYS);

        Label activityHeader = new Label("Live Activity Stream");
        activityHeader.getStyleClass().add("m3-title-medium");

        recentActivityList.setPrefHeight(250);
        recentActivityList.setPlaceholder(new Label("No recent registration or grading events logged"));

        activityCard.getChildren().addAll(activityHeader, recentActivityList);

        bottomSection.getChildren().addAll(sessionCard, activityCard);

        getChildren().addAll(titleBox, statsGrid, bottomSection);
    }

    private VBox createStatCard(String labelText, Label valueLabel, String icon) {
        VBox card = new VBox(8);
        card.getStyleClass().add("m3-card-elevated");

        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 18px;");
        Label label = new Label(labelText);
        label.getStyleClass().add("m3-body-medium");
        headerRow.getChildren().addAll(iconLabel, label);

        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: -md-sys-color-primary;");

        card.getChildren().addAll(headerRow, valueLabel);
        return card;
    }

    public void refresh() {
        List<Student> students = store.getStudentRepository().findAll();
        lblStudentsCount.setText(String.valueOf(students.size()));
        lblCoursesCount.setText(String.valueOf(store.getCourses().size()));
        lblSectionsCount.setText(String.valueOf(store.getCourseSections().size()));

        int totalCredits = students.stream()
                .mapToInt(s -> s.getAcademicRecord().getEarnedCredits())
                .sum();
        lblCreditsCount.setText(String.valueOf(totalCredits));

        LocalDate today = store.getSimulatedDate();
        lblCurrentDate.setText(today.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));

        Semester currentSemester = store.getSemesters().stream()
                .filter(s -> !today.isBefore(s.getStartDate()) && !today.isAfter(s.getEndDate()))
                .findFirst()
                .orElse(store.getSemesters().isEmpty() ? null : store.getSemesters().get(0));

        if (currentSemester != null) {
            lblCurrentTerm.setText(currentSemester.toString());
            boolean regOpen = currentSemester.isInRegistrationPeriod(today);
            lblRegStatus.setText(regOpen ? "● REGISTRATION OPEN" : "● REGISTRATION CLOSED");
            lblRegStatus.getStyleClass().removeAll("m3-badge", "m3-badge-success", "m3-badge-danger");
            lblRegStatus.getStyleClass().addAll("m3-badge", regOpen ? "m3-badge-success" : "m3-badge-danger");
        } else {
            lblCurrentTerm.setText("None");
            lblRegStatus.setText("N/A");
        }

        recentActivityList.getItems().clear();
        List<Notification> notes = store.getNotificationService().getNotifications();
        if (notes.isEmpty()) {
            recentActivityList.getItems().add("System initialized. All services operational.");
        } else {
            for (int i = notes.size() - 1; i >= Math.max(0, notes.size() - 8); i--) {
                recentActivityList.getItems().add(notes.get(i).getMessage());
            }
        }
    }
}
