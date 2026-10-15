package com.university.registration.ui;

import com.university.registration.model.Semester;
import com.university.registration.ui.context.UniversityDataStore;
import com.university.registration.ui.views.*;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Main JavaFX Application for the University Registration System.
 * Coordinates responsive sidebar navigation, global header controls with simulated date,
 * and view transitions.
 */
public class UniversityApp extends Application {

    private final UniversityDataStore store = UniversityDataStore.getInstance();
    private final BorderPane rootLayout = new BorderPane();
    private final StackPane contentContainer = new StackPane();
    private final Map<String, Node> viewCache = new HashMap<>();
    private final Map<String, Button> navButtons = new HashMap<>();

    private Label lblCurrentDate;
    private Label lblNotificationBadge;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("University Registration & Academic Management System");

        rootLayout.getStyleClass().add("app-container");

        // Top bar
        HBox topBar = createHeaderBar();
        rootLayout.setTop(topBar);

        // Sidebar navigation
        VBox sidebar = createSidebar();
        rootLayout.setLeft(sidebar);

        // Center content area
        contentContainer.getStyleClass().add("content-area");
        rootLayout.setCenter(contentContainer);

        // Switch to initial view
        navigateTo("dashboard");

        Scene scene = new Scene(rootLayout, 1280, 820);
        // Load CSS stylesheet
        String cssPath = getClass().getResource("/styles/app.css") != null
                ? getClass().getResource("/styles/app.css").toExternalForm()
                : null;
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        }

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.show();

        // Listen for store changes to update notifications badge and date
        store.addChangeListener(this::updateHeaderIndicators);
        updateHeaderIndicators();
    }

    private HBox createHeaderBar() {
        HBox header = new HBox(16);
        header.getStyleClass().add("header-bar");
        header.setAlignment(Pos.CENTER_RIGHT);

        // Left spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Simulated date controls widget
        HBox dateWidget = new HBox(10);
        dateWidget.setAlignment(Pos.CENTER_LEFT);
        dateWidget.setStyle("-fx-background-color: #1a2333; -fx-padding: 6px 14px; -fx-background-radius: 8px; -fx-border-color: #273852; -fx-border-radius: 8px;");

        Label dateIcon = new Label("📅");
        Label datePrefix = new Label("System Date:");
        datePrefix.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: 500; -fx-font-size: 11px;");

        lblCurrentDate = new Label();
        lblCurrentDate.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 12px;");

        DatePicker datePicker = new DatePicker(store.getSimulatedDate());
        datePicker.setPrefWidth(125);
        datePicker.valueProperty().addListener((obs, oldV, newDate) -> {
            if (newDate != null) {
                store.setSimulatedDate(newDate);
            }
        });

        // Quick simulation presets
        MenuButton presetMenu = new MenuButton("Date Presets");
        presetMenu.getStyleClass().add("btn-secondary");

        MenuItem presetReg = new MenuItem("Fall Reg Open (Aug 15, 2025)");
        presetReg.setOnAction(e -> {
            datePicker.setValue(LocalDate.of(2025, 8, 15));
        });

        MenuItem presetTermStart = new MenuItem("Fall Classes Start (Sep 10, 2025)");
        presetTermStart.setOnAction(e -> {
            datePicker.setValue(LocalDate.of(2025, 9, 10));
        });

        MenuItem presetWithdrawDeadline = new MenuItem("After Withdraw Deadline (Nov 1, 2025)");
        presetWithdrawDeadline.setOnAction(e -> {
            datePicker.setValue(LocalDate.of(2025, 11, 1));
        });

        MenuItem presetSpringReg = new MenuItem("Spring Reg Open (Jan 15, 2026)");
        presetSpringReg.setOnAction(e -> {
            datePicker.setValue(LocalDate.of(2026, 1, 15));
        });

        presetMenu.getItems().addAll(presetReg, presetTermStart, presetWithdrawDeadline, presetSpringReg);

        dateWidget.getChildren().addAll(dateIcon, datePrefix, lblCurrentDate, datePicker, presetMenu);

        // Header notifications bell shortcut
        Button btnBell = new Button("🔔");
        btnBell.getStyleClass().add("btn-secondary");
        lblNotificationBadge = new Label("0");
        lblNotificationBadge.getStyleClass().addAll("badge", "badge-info");
        btnBell.setGraphic(lblNotificationBadge);
        btnBell.setContentDisplay(ContentDisplay.RIGHT);
        btnBell.setOnAction(e -> navigateTo("notifications"));

        header.getChildren().addAll(spacer, dateWidget, btnBell);
        return header;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(6);
        sidebar.getStyleClass().add("sidebar");

        // Brand
        VBox brandBox = new VBox(2);
        brandBox.getStyleClass().add("brand-container");
        Label brandTitle = new Label("Apex University");
        brandTitle.getStyleClass().add("brand-title");
        Label brandSub = new Label("Registration & Academic System");
        brandSub.getStyleClass().add("brand-subtitle");
        brandBox.getChildren().addAll(brandTitle, brandSub);

        // Navigation sections
        Label navSecCore = new Label("MAIN");
        navSecCore.getStyleClass().add("nav-section-title");

        Button btnDashboard = createNavButton("dashboard", "📊  Dashboard");
        Button btnStudents = createNavButton("students", "👥  Student Directory");
        Button btnCourses = createNavButton("courses", "📚  Courses & Schedules");

        Label navSecAcademics = new Label("OPERATIONS");
        navSecAcademics.getStyleClass().add("nav-section-title");

        Button btnRegistration = createNavButton("registration", "📝  Course Registration");
        Button btnGrading = createNavButton("grading", "🎓  Instructor Grading");

        Label navSecAudits = new Label("RECORDS & AUDITS");
        navSecAudits.getStyleClass().add("nav-section-title");

        Button btnTranscript = createNavButton("transcript", "📄  Academic Transcripts");
        Button btnGraduation = createNavButton("graduation", "🏆  Graduation Checker");
        Button btnNotifications = createNavButton("notifications", "🔔  System Notifications");

        sidebar.getChildren().addAll(
                brandBox,
                navSecCore,
                btnDashboard,
                btnStudents,
                btnCourses,
                navSecAcademics,
                btnRegistration,
                btnGrading,
                navSecAudits,
                btnTranscript,
                btnGraduation,
                btnNotifications
        );

        return sidebar;
    }

    private Button createNavButton(String viewId, String label) {
        Button btn = new Button(label);
        btn.getStyleClass().add("nav-button");
        btn.setOnAction(e -> navigateTo(viewId));
        navButtons.put(viewId, btn);
        return btn;
    }

    private void navigateTo(String viewId) {
        // Highlight active nav button
        for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
            if (entry.getKey().equals(viewId)) {
                if (!entry.getValue().getStyleClass().contains("nav-button-active")) {
                    entry.getValue().getStyleClass().add("nav-button-active");
                }
            } else {
                entry.getValue().getStyleClass().remove("nav-button-active");
            }
        }

        // Lazy-load view into container
        Node view = viewCache.computeIfAbsent(viewId, this::createViewInstance);
        contentContainer.getChildren().setAll(view);
    }

    private Node createViewInstance(String viewId) {
        return switch (viewId) {
            case "dashboard" -> new DashboardView();
            case "students" -> new StudentsView();
            case "courses" -> new CoursesView();
            case "registration" -> new RegistrationView();
            case "grading" -> new GradingView();
            case "transcript" -> new TranscriptView();
            case "graduation" -> new GraduationView();
            case "notifications" -> new NotificationsView();
            default -> new Label("View not found: " + viewId);
        };
    }

    private void updateHeaderIndicators() {
        if (lblCurrentDate != null) {
            LocalDate today = store.getSimulatedDate();
            lblCurrentDate.setText(today.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        }
        if (lblNotificationBadge != null) {
            int count = store.getNotificationService().getNotifications().size();
            lblNotificationBadge.setText(String.valueOf(count));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
