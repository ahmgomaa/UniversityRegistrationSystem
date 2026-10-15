package com.university.registration.ui.views;

import com.university.registration.model.*;
import com.university.registration.ui.context.UniversityDataStore;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Course catalog and section schedule view with capacity monitoring
 * and creation dialogs.
 */
public class CoursesView extends VBox {

    private final UniversityDataStore store = UniversityDataStore.getInstance();
    private final TableView<Course> coursesTable = new TableView<>();
    private final TableView<CourseSection> sectionsTable = new TableView<>();
    private final TabPane tabPane = new TabPane();

    public CoursesView() {
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
        Label title = new Label("Courses & Schedule");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Explore course catalog, prerequisites, active sections, and classroom schedules");
        subtitle.getStyleClass().add("header-subtitle");
        titleBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button btnNewCourse = new Button("+ New Course");
        btnNewCourse.getStyleClass().add("btn-secondary");
        btnNewCourse.setOnAction(e -> showCreateCourseDialog());

        Button btnNewSection = new Button("+ New Section");
        btnNewSection.getStyleClass().add("btn-primary");
        btnNewSection.setOnAction(e -> showCreateSectionDialog());

        topBar.getChildren().addAll(titleBox, btnNewCourse, btnNewSection);

        // Tabs: Course Catalog & Section Schedules
        setupCoursesTable();
        setupSectionsTable();

        Tab tabCatalog = new Tab("Course Catalog", coursesTable);
        tabCatalog.setClosable(false);

        Tab tabSections = new Tab("Active Sections & Schedules", sectionsTable);
        tabSections.setClosable(false);

        tabPane.getTabs().addAll(tabCatalog, tabSections);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        getChildren().addAll(topBar, tabPane);
    }

    private void setupCoursesTable() {
        TableColumn<Course, String> colCode = new TableColumn<>("COURSE CODE");
        colCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourseCode()));
        colCode.setPrefWidth(120);

        TableColumn<Course, String> colName = new TableColumn<>("COURSE NAME");
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourseName()));
        colName.setPrefWidth(260);

        TableColumn<Course, String> colCredits = new TableColumn<>("CREDITS");
        colCredits.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCredits())));
        colCredits.setPrefWidth(80);

        TableColumn<Course, String> colPrereqs = new TableColumn<>("PREREQUISITES");
        colPrereqs.setCellValueFactory(data -> {
            String prereqs = data.getValue().getPrerequisites().stream()
                    .map(Course::getCourseCode)
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(prereqs.isEmpty() ? "None" : prereqs);
        });
        colPrereqs.setPrefWidth(220);

        TableColumn<Course, String> colDesc = new TableColumn<>("DESCRIPTION");
        colDesc.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        colDesc.setPrefWidth(340);

        coursesTable.getColumns().addAll(colCode, colName, colCredits, colPrereqs, colDesc);
    }

    private void setupSectionsTable() {
        TableColumn<CourseSection, String> colSecId = new TableColumn<>("SECTION ID");
        colSecId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSectionId()));
        colSecId.setPrefWidth(140);

        TableColumn<CourseSection, String> colCourse = new TableColumn<>("COURSE");
        colCourse.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCourse().getCourseCode() + " - " + data.getValue().getCourse().getCourseName()));
        colCourse.setPrefWidth(260);

        TableColumn<CourseSection, String> colSemester = new TableColumn<>("SEMESTER");
        colSemester.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSemester().toString()));
        colSemester.setPrefWidth(120);

        TableColumn<CourseSection, String> colInstructor = new TableColumn<>("INSTRUCTOR");
        colInstructor.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getInstructor() != null ? data.getValue().getInstructor().getName() : "Unassigned"));
        colInstructor.setPrefWidth(160);

        TableColumn<CourseSection, String> colMeetings = new TableColumn<>("MEETING SCHEDULE");
        colMeetings.setCellValueFactory(data -> {
            String meetings = data.getValue().getMeetingTimes().stream()
                    .map(m -> m.getDay().name().substring(0, 3) + " " + m.getStartTime() + "-" + m.getEndTime())
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(meetings.isEmpty() ? "TBA" : meetings);
        });
        colMeetings.setPrefWidth(220);

        TableColumn<CourseSection, String> colCapacity = new TableColumn<>("SEATS");
        colCapacity.setCellValueFactory(data -> {
            int enrolled = (int) data.getValue().getEnrollments().stream().filter(Enrollment::isActive).count();
            return new SimpleStringProperty(enrolled + " / " + data.getValue().getCapacity()
                    + " (" + data.getValue().getAvailableSeatsCount() + " free)");
        });
        colCapacity.setPrefWidth(140);

        sectionsTable.getColumns().addAll(colSecId, colCourse, colSemester, colInstructor, colMeetings, colCapacity);
    }

    private void showCreateCourseDialog() {
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Create New Course");
        dialog.setHeaderText("Add a new course to the university catalog");

        ButtonType createBtn = new ButtonType("Create Course", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField txtCode = new TextField();
        txtCode.setPromptText("e.g. CS250");
        TextField txtName = new TextField();
        txtName.setPromptText("e.g. Web Development");
        TextField txtCredits = new TextField("3");
        TextArea txtDesc = new TextArea();
        txtDesc.setPromptText("Course description...");
        txtDesc.setPrefRowCount(3);

        ListView<Course> prereqList = new ListView<>(FXCollections.observableArrayList(store.getCourses()));
        prereqList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        prereqList.setPrefHeight(100);
        prereqList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCourseCode() + " - " + item.getCourseName());
            }
        });

        grid.add(new Label("Course Code:"), 0, 0);
        grid.add(txtCode, 1, 0);
        grid.add(new Label("Course Name:"), 0, 1);
        grid.add(txtName, 1, 1);
        grid.add(new Label("Credit Hours:"), 0, 2);
        grid.add(txtCredits, 1, 2);
        grid.add(new Label("Description:"), 0, 3);
        grid.add(txtDesc, 1, 3);
        grid.add(new Label("Prerequisites:"), 0, 4);
        grid.add(prereqList, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == createBtn) {
                if (txtCode.getText().isBlank() || txtName.getText().isBlank()) return null;
                int credits = 3;
                try {
                    credits = Integer.parseInt(txtCredits.getText().trim());
                } catch (NumberFormatException ignored) {}

                Course c = new Course(txtCode.getText().trim(), txtName.getText().trim(), credits, txtDesc.getText());
                for (Course p : prereqList.getSelectionModel().getSelectedItems()) {
                    c.addPrerequisite(p);
                }
                store.addCourse(c);
                return c;
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showCreateSectionDialog() {
        Dialog<CourseSection> dialog = new Dialog<>();
        dialog.setTitle("Schedule Course Section");
        dialog.setHeaderText("Create a new scheduled section for an upcoming semester");

        ButtonType createBtn = new ButtonType("Schedule Section", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField txtSecId = new TextField("SEC-" + (store.getCourseSections().size() + 1));
        ComboBox<Course> courseCombo = new ComboBox<>(FXCollections.observableArrayList(store.getCourses()));
        if (!store.getCourses().isEmpty()) courseCombo.getSelectionModel().selectFirst();
        courseCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Course c) { return c != null ? c.getCourseCode() + " - " + c.getCourseName() : ""; }
            @Override public Course fromString(String s) { return null; }
        });

        ComboBox<Semester> semesterCombo = new ComboBox<>(FXCollections.observableArrayList(store.getSemesters()));
        if (!store.getSemesters().isEmpty()) semesterCombo.getSelectionModel().selectFirst();

        ComboBox<Instructor> instructorCombo = new ComboBox<>(FXCollections.observableArrayList(store.getInstructors()));
        if (!store.getInstructors().isEmpty()) instructorCombo.getSelectionModel().selectFirst();
        instructorCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Instructor i) { return i != null ? i.getName() : ""; }
            @Override public Instructor fromString(String s) { return null; }
        });

        TextField txtCap = new TextField("30");

        grid.add(new Label("Section ID:"), 0, 0);
        grid.add(txtSecId, 1, 0);
        grid.add(new Label("Course:"), 0, 1);
        grid.add(courseCombo, 1, 1);
        grid.add(new Label("Semester:"), 0, 2);
        grid.add(semesterCombo, 1, 2);
        grid.add(new Label("Instructor:"), 0, 3);
        grid.add(instructorCombo, 1, 3);
        grid.add(new Label("Capacity:"), 0, 4);
        grid.add(txtCap, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == createBtn) {
                int cap = 30;
                try {
                    cap = Integer.parseInt(txtCap.getText().trim());
                } catch (NumberFormatException ignored) {}

                CourseSection sec = new CourseSection(
                        txtSecId.getText().trim(),
                        courseCombo.getValue(),
                        semesterCombo.getValue(),
                        instructorCombo.getValue(),
                        cap
                );
                // Default meeting time
                sec.addMeetingTime(new MeetingTime(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 30)));
                sec.addMeetingTime(new MeetingTime(DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(10, 30)));

                store.addCourseSection(sec);
                return sec;
            }
            return null;
        });

        dialog.showAndWait();
    }

    public void refresh() {
        coursesTable.setItems(FXCollections.observableArrayList(store.getCourses()));
        sectionsTable.setItems(FXCollections.observableArrayList(store.getCourseSections()));
    }
}
