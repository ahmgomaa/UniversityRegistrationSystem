package com.university.registration.ui.views;

import com.university.registration.dto.Notification;
import com.university.registration.model.Student;
import com.university.registration.ui.context.UniversityDataStore;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * System Notifications Center & Audit Trail.
 * Built with Google Material Design 3 (M3) components matching gym-management-system m3fx style.
 */
public class NotificationsView extends VBox {

    private final UniversityDataStore store = UniversityDataStore.getInstance();
    private final ObservableList<String> notificationsList = FXCollections.observableArrayList();
    private final ListView<String> listView = new ListView<>();
    private final TextField searchField = new TextField();
    private final Label lblCountChip = new Label("0 events");
    private String categoryFilter = "ALL";
    private final ObservableList<Button> filterChips = FXCollections.observableArrayList();

    public NotificationsView() {
        setSpacing(20);
        setPadding(new Insets(10, 0, 24, 0));

        initView();
        refresh();
        store.addChangeListener(this::refresh);
    }

    private void initView() {
        // Page header & Action buttons
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("System Notifications & Audit Log");
        title.getStyleClass().add("m3-headline-medium");
        Label subtitle = new Label("Chronological audit journal of registrations, grade submissions, and academic status alerts");
        subtitle.getStyleClass().add("m3-body-medium");
        titleBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button btnSendTest = new Button("📢 Send Test Broadcast");
        btnSendTest.getStyleClass().addAll("m3-button", "m3-button-tonal");
        btnSendTest.setOnAction(e -> {
            Student s = store.getStudentRepository().findAll().stream().findFirst().orElse(null);
            if (s != null) {
                store.getNotificationService().notify(s, "System broadcast: Midterm course evaluations are now live.");
                store.notifyStateChanged();
            }
        });

        Button btnClear = new Button("🗑️ Clear Log");
        btnClear.getStyleClass().addAll("m3-button", "m3-button-outlined");
        btnClear.setOnAction(e -> {
            store.getNotificationService().clearNotifications();
            store.notifyStateChanged();
        });

        topBar.getChildren().addAll(titleBox, btnSendTest, btnClear);

        // Filter & Search Controls
        HBox filterRow = new HBox(16);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        // M3 Pill Search Bar
        HBox searchBar = new HBox(10);
        searchBar.getStyleClass().add("m3-search-bar");
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPrefWidth(380);

        Label searchIcon = new Label("🔍");
        searchField.getStyleClass().add("m3-search-field");
        searchField.setPromptText("Filter notification events...");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchBar.getChildren().addAll(searchIcon, searchField);

        // M3 Filter Chips
        HBox chipsBox = new HBox(8);
        chipsBox.setAlignment(Pos.CENTER_LEFT);
        Button chipAll = createFilterChip("All Events", "ALL");
        Button chipReg = createFilterChip("Registrations", "REG");
        Button chipGrade = createFilterChip("Grading", "GRADE");
        Button chipStanding = createFilterChip("Standing Alerts", "STATUS");
        chipsBox.getChildren().addAll(chipAll, chipReg, chipGrade, chipStanding);

        lblCountChip.getStyleClass().addAll("m3-chip", "m3-chip-selected");

        filterRow.getChildren().addAll(searchBar, chipsBox, lblCountChip);

        // Filtered List View
        FilteredList<String> filteredData = new FilteredList<>(notificationsList, p -> true);

        Runnable updateFilter = () -> {
            String query = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
            filteredData.setPredicate(note -> {
                if (note == null) return false;
                String lower = note.toLowerCase();

                // Search query filter
                if (!query.isEmpty() && !lower.contains(query)) {
                    return false;
                }

                // Category filter
                switch (categoryFilter) {
                    case "REG":
                        return lower.contains("registered") || lower.contains("dropped") || lower.contains("waitlist") || lower.contains("enroll");
                    case "GRADE":
                        return lower.contains("grade") || lower.contains("submitted") || lower.contains("gpa") || lower.contains("score");
                    case "STATUS":
                        return lower.contains("probation") || lower.contains("suspended") || lower.contains("standing") || lower.contains("advisor");
                    default:
                        return true;
                }
            });
        };

        searchField.textProperty().addListener((obs, oldV, newV) -> updateFilter.run());

        // Elevated card containing list
        VBox listCard = new VBox(12);
        listCard.getStyleClass().add("m3-card-elevated");
        VBox.setVgrow(listCard, Priority.ALWAYS);

        listView.setItems(filteredData);
        listView.setPlaceholder(new Label("No system notifications logged yet"));
        VBox.setVgrow(listView, Priority.ALWAYS);

        // Custom Cell Factory for M3 formatting
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox row = new HBox(12);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(6, 4, 6, 4));

                    String icon = "ℹ️";
                    String lower = item.toLowerCase();
                    if (lower.contains("grade") || lower.contains("score")) icon = "📝";
                    else if (lower.contains("registered") || lower.contains("enroll")) icon = "📘";
                    else if (lower.contains("dropped")) icon = "⚠️";
                    else if (lower.contains("probation") || lower.contains("suspended")) icon = "🚨";
                    else if (lower.contains("advisor")) icon = "👤";
                    else if (lower.contains("broadcast")) icon = "📢";

                    Label iconLbl = new Label(icon);
                    iconLbl.setStyle("-fx-font-size: 16px;");

                    Label textLbl = new Label(item);
                    textLbl.setWrapText(true);
                    textLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: -md-sys-color-on-surface;");
                    HBox.setHgrow(textLbl, Priority.ALWAYS);

                    row.getChildren().addAll(iconLbl, textLbl);
                    setGraphic(row);
                    setText(null);
                }
            }
        });

        listCard.getChildren().add(listView);

        getChildren().addAll(topBar, filterRow, listCard);
    }

    private Button createFilterChip(String text, String category) {
        Button chip = new Button(text);
        chip.getStyleClass().add("m3-chip");
        if ("ALL".equals(category)) {
            chip.getStyleClass().add("m3-chip-selected");
        }
        chip.setOnAction(e -> {
            categoryFilter = category;
            for (Button c : filterChips) {
                c.getStyleClass().remove("m3-chip-selected");
            }
            chip.getStyleClass().add("m3-chip-selected");
            searchField.fireEvent(new javafx.scene.input.KeyEvent(
                    javafx.scene.input.KeyEvent.KEY_RELEASED, "", "", javafx.scene.input.KeyCode.UNDEFINED,
                    false, false, false, false
            ));
            // Trigger filter update
            String q = searchField.getText();
            searchField.setText(q == null ? "" : q + " ");
            searchField.setText(q == null ? "" : q);
        });
        filterChips.add(chip);
        return chip;
    }

    public void refresh() {
        notificationsList.clear();
        List<Notification> list = store.getNotificationService().getNotifications();
        for (int i = list.size() - 1; i >= 0; i--) {
            notificationsList.add(list.get(i).getMessage());
        }
        lblCountChip.setText(list.size() + " total events");
    }
}
