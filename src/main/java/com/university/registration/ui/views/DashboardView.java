package com.university.registration.ui.views;

import com.university.registration.dto.Notification;
import com.university.registration.model.CourseSection;
import com.university.registration.model.Semester;
import com.university.registration.model.Student;
import com.university.registration.ui.context.UniversityDataStore;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Overview dashboard displaying university metrics, current academic session status,
 * and recent real-time system notifications.
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
        setPadding(new Insets(24));
        getStyleClass().add("content-area");

        initView();
        refresh();
        store.addChangeListener(this::refresh);
    }

    private void initView() {
        // Page header
        VBox titleBox = new VBox(4);
        Label title = new Label("University Dashboard");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("System overview, current academic term status, and recent activity");
        subtitle.getStyleClass().add("header-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // Stats grid
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(16);
        statsGrid.setVgap(16);

        VBox cardStudents = createStatCard("Total Students", lblStudentsCount, "badge-info");
        VBox cardCourses = createStatCard("Courses Catalog", lblCoursesCount, "badge-info");
        VBox cardSections = createStatCard("Active Sections", lblSectionsCount, "badge-info");
        VBox cardCredits = createStatCard("Total Earned Credits", lblCreditsCount, "badge-success");

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

        // Academic Session Card
        VBox sessionCard = new VBox(16);
        sessionCard.getStyleClass().add("card");
        sessionCard.setPrefWidth(420);

        Label sessionHeader = new Label("Current Academic Session");
        sessionHeader.getStyleClass().add("card-title");

        GridPane sessionDetails = new GridPane();
        sessionDetails.setHgap(16);
        sessionDetails.setVgap(12);

        Label lbl1 = new Label("Simulated Date:");
        lbl1.getStyleClass().add("card-subtitle");
        Label lbl2 = new Label("Active Term:");
        lbl2.getStyleClass().add("card-subtitle");
        Label lbl3 = new Label("Registration Window:");
        lbl3.getStyleClass().add("card-subtitle");

        lblCurrentDate.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8;");
        lblCurrentTerm.setStyle("-fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        sessionDetails.add(lbl1, 0, 0);
        sessionDetails.add(lblCurrentDate, 1, 0);
        sessionDetails.add(lbl2, 0, 1);
        sessionDetails.add(lblCurrentTerm, 1, 1);
        sessionDetails.add(lbl3, 0, 2);
        sessionDetails.add(lblRegStatus, 1, 2);

        Label sessionNote = new Label(
                "💡 Tip: Use the 'Simulated Date' controls in the top bar to test registration rules, late registrations, and withdrawal deadlines.");
        sessionNote.setWrapText(true);
        sessionNote.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-padding: 8px 0 0 0;");

        sessionCard.getChildren().addAll(sessionHeader, sessionDetails, sessionNote);

        // Recent System Activity Card
        VBox activityCard = new VBox(12);
        activityCard.getStyleClass().add("card");
        HBox.setHgrow(activityCard, Priority.ALWAYS);

        Label activityHeader = new Label("Live Notifications Stream");
        activityHeader.getStyleClass().add("card-title");

        recentActivityList.setPrefHeight(240);
        recentActivityList.setPlaceholder(new Label("No recent registration or grading events recorded"));

        activityCard.getChildren().addAll(activityHeader, recentActivityList);

        bottomSection.getChildren().addAll(sessionCard, activityCard);

        getChildren().addAll(titleBox, statsGrid, bottomSection);
    }

    private VBox createStatCard(String labelText, Label valueLabel, String badgeClass) {
        VBox card = new VBox(6);
        card.getStyleClass().add("stat-card");
        valueLabel.getStyleClass().add("stat-value");

        Label label = new Label(labelText);
        label.getStyleClass().add("stat-label");

        card.getChildren().addAll(valueLabel, label);
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

        // Check active semester
        Semester currentSemester = store.getSemesters().stream()
                .filter(s -> !today.isBefore(s.getStartDate()) && !today.isAfter(s.getEndDate()))
                .findFirst()
                .orElse(store.getSemesters().isEmpty() ? null : store.getSemesters().get(0));

        if (currentSemester != null) {
            lblCurrentTerm.setText(currentSemester.toString());
            boolean regOpen = currentSemester.isInRegistrationPeriod(today);
            lblRegStatus.setText(regOpen ? "● OPEN" : "● CLOSED");
            lblRegStatus.getStyleClass().removeAll("badge", "badge-success", "badge-danger");
            lblRegStatus.getStyleClass().addAll("badge", regOpen ? "badge-success" : "badge-danger");
        } else {
            lblCurrentTerm.setText("None");
            lblRegStatus.setText("N/A");
        }

        // Recent notifications
        recentActivityList.getItems().clear();
        List<Notification> notes = store.getNotificationService().getNotifications();
        if (notes.isEmpty()) {
            recentActivityList.getItems().add("System initialized. Ready for operations.");
        } else {
            for (int i = notes.size() - 1; i >= Math.max(0, notes.size() - 8); i--) {
                recentActivityList.getItems().add(notes.get(i).getMessage());
            }
        }
    }
}
