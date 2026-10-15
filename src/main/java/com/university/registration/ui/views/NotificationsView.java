package com.university.registration.ui.views;

import com.university.registration.dto.Notification;
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
 * System Notifications Center.
 * Displays real-time notifications dispatched during registration, grading, and standing updates.
 */
public class NotificationsView extends VBox {

    private final UniversityDataStore store = UniversityDataStore.getInstance();
    private final ObservableList<String> notificationsList = FXCollections.observableArrayList();
    private final ListView<String> listView = new ListView<>();
    private final TextField searchField = new TextField();
    private final Label lblCount = new Label("0 notifications");

    public NotificationsView() {
        setSpacing(20);
        setPadding(new Insets(24));
        getStyleClass().add("content-area");

        initView();
        refresh();
        store.addChangeListener(this::refresh);
    }

    private void initView() {
        // Page header & Action buttons
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("System Notifications");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Audit trail of notifications dispatched to students and academic staff");
        subtitle.getStyleClass().add("header-subtitle");
        titleBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button btnClear = new Button("Clear Log");
        btnClear.getStyleClass().add("btn-secondary");
        btnClear.setOnAction(e -> {
            store.getNotificationService().clearNotifications();
            store.notifyStateChanged();
        });

        topBar.getChildren().addAll(titleBox, btnClear);

        // Search bar & count
        HBox filterBar = new HBox(16);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("🔍 Filter notifications...");
        searchField.setPrefWidth(350);

        lblCount.getStyleClass().add("card-subtitle");

        filterBar.getChildren().addAll(searchField, lblCount);

        // List View setup
        FilteredList<String> filteredData = new FilteredList<>(notificationsList, p -> true);
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            filteredData.setPredicate(note -> {
                if (newV == null || newV.trim().isEmpty()) return true;
                return note.toLowerCase().contains(newV.toLowerCase().trim());
            });
        });

        listView.setItems(filteredData);
        listView.setPlaceholder(new Label("No system notifications logged yet"));
        VBox.setVgrow(listView, Priority.ALWAYS);

        getChildren().addAll(topBar, filterBar, listView);
    }

    public void refresh() {
        notificationsList.clear();
        List<Notification> list = store.getNotificationService().getNotifications();
        for (int i = list.size() - 1; i >= 0; i--) {
            notificationsList.add(list.get(i).getMessage());
        }
        lblCount.setText(list.size() + " total notifications");
    }
}
