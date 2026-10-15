package com.university.registration.ui;

import com.university.registration.ui.context.UniversityDataStore;
import com.university.registration.ui.theme.M3Palette;
import com.university.registration.ui.theme.ThemeManager;
import com.university.registration.ui.views.*;

import javafx.application.Application;
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
 * Styled with Material Design 3 (M3) components, live Light/Dark mode toggling,
 * dynamic theme seed palettes, and navigation drawer.
 */
public class UniversityApp extends Application {

    private final UniversityDataStore store = UniversityDataStore.getInstance();
    private final ThemeManager themeManager = ThemeManager.getInstance();

    private final BorderPane rootLayout = new BorderPane();
    private final StackPane contentContainer = new StackPane();
    private final Map<String, Node> viewCache = new HashMap<>();
    private final Map<String, Button> navButtons = new HashMap<>();

    private Scene mainScene;
    private Label lblCurrentDate;
    private Label lblNotificationBadge;
    private Button btnThemeToggle;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("University Registration & Academic Management — Material 3");

        // Top bar
        HBox topBar = createTopAppBar();
        rootLayout.setTop(topBar);

        // M3 Navigation drawer
        VBox navDrawer = createNavDrawer();
        rootLayout.setLeft(navDrawer);

        // Center content area
        contentContainer.setStyle("-fx-padding: 24px;");
        rootLayout.setCenter(contentContainer);

        // Switch to initial view
        navigateTo("dashboard");

        mainScene = new Scene(rootLayout, 1320, 840);

        // Apply dynamic M3 theme
        themeManager.applyTheme(mainScene);

        primaryStage.setScene(mainScene);
        primaryStage.setMinWidth(1050);
        primaryStage.setMinHeight(720);
        primaryStage.show();

        store.addChangeListener(this::updateHeaderIndicators);
        updateHeaderIndicators();
    }

    private HBox createTopAppBar() {
        HBox bar = new HBox(16);
        bar.getStyleClass().add("m3-top-app-bar");
        bar.setAlignment(Pos.CENTER_RIGHT);

        // Left Brand title
        VBox brandBox = new VBox(2);
        Label title = new Label("Apex University");
        title.getStyleClass().add("m3-app-title");
        Label subtitle = new Label("Academic Management & Registration System");
        subtitle.getStyleClass().add("m3-app-subtitle");
        brandBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Simulated date controls widget
        HBox dateWidget = new HBox(8);
        dateWidget.setAlignment(Pos.CENTER_LEFT);
        dateWidget.getStyleClass().add("m3-chip");

        Label dateIcon = new Label("📅");
        lblCurrentDate = new Label();
        lblCurrentDate.setStyle("-fx-font-weight: bold; -fx-text-fill: -md-sys-color-primary;");

        DatePicker datePicker = new DatePicker(store.getSimulatedDate());
        datePicker.setPrefWidth(120);
        datePicker.valueProperty().addListener((obs, oldV, newDate) -> {
            if (newDate != null) {
                store.setSimulatedDate(newDate);
            }
        });

        MenuButton presetMenu = new MenuButton("Date Presets");
        presetMenu.getStyleClass().addAll("m3-button", "m3-button-text");

        MenuItem presetReg = new MenuItem("Fall Reg Open (Aug 15, 2025)");
        presetReg.setOnAction(e -> datePicker.setValue(LocalDate.of(2025, 8, 15)));

        MenuItem presetTermStart = new MenuItem("Fall Term Start (Sep 10, 2025)");
        presetTermStart.setOnAction(e -> datePicker.setValue(LocalDate.of(2025, 9, 10)));

        MenuItem presetWithdrawDeadline = new MenuItem("After Withdraw Deadline (Nov 1, 2025)");
        presetWithdrawDeadline.setOnAction(e -> datePicker.setValue(LocalDate.of(2025, 11, 1)));

        MenuItem presetSpringReg = new MenuItem("Spring Reg Open (Jan 15, 2026)");
        presetSpringReg.setOnAction(e -> datePicker.setValue(LocalDate.of(2026, 1, 15)));

        presetMenu.getItems().addAll(presetReg, presetTermStart, presetWithdrawDeadline, presetSpringReg);
        dateWidget.getChildren().addAll(dateIcon, lblCurrentDate, datePicker, presetMenu);

        // M3 Seed Color Picker
        ComboBox<M3Palette.SeedColor> seedCombo = new ComboBox<>();
        seedCombo.getItems().addAll(M3Palette.SeedColor.values());
        seedCombo.setValue(themeManager.getSeedColor());
        seedCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(M3Palette.SeedColor s) { return s != null ? "🎨 " + s.getDisplayName() : ""; }
            @Override public M3Palette.SeedColor fromString(String s) { return null; }
        });
        seedCombo.valueProperty().addListener((obs, oldV, newSeed) -> {
            if (newSeed != null) {
                themeManager.setSeedColor(newSeed);
                if (mainScene != null) themeManager.applyTheme(mainScene);
            }
        });

        // M3 Light / Dark Mode Toggle Button
        btnThemeToggle = new Button("🌙 Dark");
        btnThemeToggle.getStyleClass().addAll("m3-button", "m3-button-tonal");
        btnThemeToggle.setOnAction(e -> {
            themeManager.toggleDarkMode();
            btnThemeToggle.setText(themeManager.isDarkMode() ? "🌙 Dark" : "☀️ Light");
            if (mainScene != null) themeManager.applyTheme(mainScene);
        });

        // Notifications Bell Button
        Button btnBell = new Button("🔔");
        btnBell.getStyleClass().addAll("m3-button", "m3-button-outlined");
        lblNotificationBadge = new Label("0");
        lblNotificationBadge.getStyleClass().addAll("m3-badge", "m3-badge-info");
        btnBell.setGraphic(lblNotificationBadge);
        btnBell.setContentDisplay(ContentDisplay.RIGHT);
        btnBell.setOnAction(e -> navigateTo("notifications"));

        bar.getChildren().addAll(brandBox, spacer, dateWidget, seedCombo, btnThemeToggle, btnBell);
        return bar;
    }

    private VBox createNavDrawer() {
        VBox drawer = new VBox(6);
        drawer.getStyleClass().add("m3-nav-drawer");

        Label navSecCore = new Label("CORE PLATFORM");
        navSecCore.getStyleClass().add("m3-nav-section-title");

        Button btnDashboard = createNavButton("dashboard", "📊   Dashboard");
        Button btnStudents = createNavButton("students", "👥   Student Directory");
        Button btnCourses = createNavButton("courses", "📚   Courses & Schedules");

        Label navSecOperations = new Label("REGISTRATION & GRADING");
        navSecOperations.getStyleClass().add("m3-nav-section-title");

        Button btnRegistration = createNavButton("registration", "📝   Course Registration");
        Button btnGrading = createNavButton("grading", "🎓   Instructor Grading");

        Label navSecAudits = new Label("RECORDS & AUDITS");
        navSecAudits.getStyleClass().add("m3-nav-section-title");

        Button btnTranscript = createNavButton("transcript", "📄   Transcripts & GPA");
        Button btnGraduation = createNavButton("graduation", "🏆   Graduation Checker");
        Button btnNotifications = createNavButton("notifications", "🔔   System Notifications");

        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        // Bottom User Profile Card
        VBox userCard = new VBox(4);
        userCard.getStyleClass().add("m3-card-filled");
        userCard.setStyle("-fx-padding: 12px; -fx-background-radius: 12px;");

        Label lblUser = new Label("Sarah Connor");
        lblUser.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: -md-sys-color-on-surface;");
        Label lblRole = new Label("● REGISTRAR");
        lblRole.getStyleClass().addAll("m3-badge", "m3-badge-info");
        userCard.getChildren().addAll(lblUser, lblRole);

        drawer.getChildren().addAll(
                navSecCore,
                btnDashboard,
                btnStudents,
                btnCourses,
                navSecOperations,
                btnRegistration,
                btnGrading,
                navSecAudits,
                btnTranscript,
                btnGraduation,
                btnNotifications,
                bottomSpacer,
                userCard
        );

        return drawer;
    }

    private Button createNavButton(String viewId, String label) {
        Button btn = new Button(label);
        btn.getStyleClass().add("m3-nav-item");
        btn.setOnAction(e -> navigateTo(viewId));
        navButtons.put(viewId, btn);
        return btn;
    }

    private void navigateTo(String viewId) {
        // Highlight active nav button with M3 active indicator pill
        for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
            if (entry.getKey().equals(viewId)) {
                if (!entry.getValue().getStyleClass().contains("m3-nav-item-active")) {
                    entry.getValue().getStyleClass().add("m3-nav-item-active");
                }
            } else {
                entry.getValue().getStyleClass().remove("m3-nav-item-active");
            }
        }

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
