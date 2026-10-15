package com.university.registration.ui.views;

import com.university.registration.model.AcademicStaff;
import com.university.registration.model.Program;
import com.university.registration.model.Student;
import com.university.registration.model.enums.AcademicStatus;
import com.university.registration.model.enums.StaffRole;
import com.university.registration.ui.context.UniversityDataStore;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Student Directory view with searching, profile details,
 * registrar academic standing updates, and advisor assignments.
 */
public class StudentsView extends VBox {

    private final UniversityDataStore store = UniversityDataStore.getInstance();
    private final ObservableList<Student> studentsMasterList = FXCollections.observableArrayList();
    private final TableView<Student> studentsTable = new TableView<>();
    private final TextField searchField = new TextField();

    // Student detail panel
    private final VBox detailPanel = new VBox(14);
    private final Label lblDetailName = new Label("Select a student");
    private final Label lblDetailNumber = new Label("-");
    private final Label lblDetailEmail = new Label("-");
    private final Label lblDetailProgram = new Label("-");
    private final Label lblDetailStatus = new Label("-");
    private final Label lblDetailGPA = new Label("-");
    private final Label lblDetailCredits = new Label("-");
    private final Label lblDetailAdvisor = new Label("-");
    private final Button btnChangeStanding = new Button("Update Standing (Registrar)");
    private final Button btnAssignAdvisor = new Button("Assign Advisor");

    public StudentsView() {
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
        Label title = new Label("Student Directory");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Manage enrolled students, academic standing, and degree programs");
        subtitle.getStyleClass().add("header-subtitle");
        titleBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button btnNewStudent = new Button("+ Add Student");
        btnNewStudent.getStyleClass().add("btn-primary");
        btnNewStudent.setOnAction(e -> showAddStudentDialog());

        topBar.getChildren().addAll(titleBox, btnNewStudent);

        // Filter / Search bar
        searchField.setPromptText("🔍 Search student by name or student ID...");
        searchField.setPrefWidth(350);

        // Table setup
        setupTable();

        // Details Side Panel setup
        setupDetailPanel();

        // Main layout: Split between Table (left) and Details panel (right)
        HBox mainSplit = new HBox(20);
        HBox.setHgrow(studentsTable, Priority.ALWAYS);
        mainSplit.getChildren().addAll(studentsTable, detailPanel);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);

        getChildren().addAll(topBar, searchField, mainSplit);
    }

    private void setupTable() {
        TableColumn<Student, String> colNum = new TableColumn<>("STUDENT ID");
        colNum.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentNumber()));
        colNum.setPrefWidth(120);

        TableColumn<Student, String> colName = new TableColumn<>("NAME");
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colName.setPrefWidth(160);

        TableColumn<Student, String> colEmail = new TableColumn<>("EMAIL");
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colEmail.setPrefWidth(190);

        TableColumn<Student, String> colProgram = new TableColumn<>("PROGRAM");
        colProgram.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProgram().getName()));
        colProgram.setPrefWidth(180);

        TableColumn<Student, String> colStatus = new TableColumn<>("STATUS");
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAcademicStatus().name()));
        colStatus.setPrefWidth(110);
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
                    if ("ACTIVE".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-active");
                    } else if ("PROBATION".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-probation");
                    } else {
                        badge.getStyleClass().add("badge-suspended");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TableColumn<Student, String> colGPA = new TableColumn<>("CGPA");
        colGPA.setCellValueFactory(data -> new SimpleStringProperty(
                String.format("%.2f", data.getValue().getAcademicRecord().calculateCGPA())));
        colGPA.setPrefWidth(80);

        TableColumn<Student, String> colCredits = new TableColumn<>("CREDITS");
        colCredits.setCellValueFactory(data -> new SimpleStringProperty(
                String.valueOf(data.getValue().getAcademicRecord().getEarnedCredits())));
        colCredits.setPrefWidth(80);

        studentsTable.getColumns().addAll(colNum, colName, colEmail, colProgram, colStatus, colGPA, colCredits);

        FilteredList<Student> filteredData = new FilteredList<>(studentsMasterList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(student -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String lower = newValue.toLowerCase().trim();
                return student.getName().toLowerCase().contains(lower)
                        || student.getStudentNumber().toLowerCase().contains(lower)
                        || student.getEmail().toLowerCase().contains(lower);
            });
        });
        studentsTable.setItems(filteredData);

        studentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            displayStudentDetails(selected);
        });
    }

    private void setupDetailPanel() {
        detailPanel.getStyleClass().add("card");
        detailPanel.setPrefWidth(320);
        detailPanel.setMinWidth(300);

        lblDetailName.getStyleClass().add("card-title");
        lblDetailName.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        grid.add(new Label("Student #:"), 0, 0);
        grid.add(lblDetailNumber, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(lblDetailEmail, 1, 1);
        grid.add(new Label("Program:"), 0, 2);
        grid.add(lblDetailProgram, 1, 2);
        grid.add(new Label("Standing:"), 0, 3);
        grid.add(lblDetailStatus, 1, 3);
        grid.add(new Label("CGPA:"), 0, 4);
        grid.add(lblDetailGPA, 1, 4);
        grid.add(new Label("Credits:"), 0, 5);
        grid.add(lblDetailCredits, 1, 5);
        grid.add(new Label("Advisor:"), 0, 6);
        grid.add(lblDetailAdvisor, 1, 6);

        for (int i = 0; i < grid.getChildren().size(); i += 2) {
            grid.getChildren().get(i).getStyleClass().add("card-subtitle");
        }

        btnChangeStanding.getStyleClass().add("btn-secondary");
        btnChangeStanding.setMaxWidth(Double.MAX_VALUE);
        btnChangeStanding.setOnAction(e -> showChangeStandingDialog());
        btnChangeStanding.setDisable(true);

        btnAssignAdvisor.getStyleClass().add("btn-secondary");
        btnAssignAdvisor.setMaxWidth(Double.MAX_VALUE);
        btnAssignAdvisor.setOnAction(e -> showAssignAdvisorDialog());
        btnAssignAdvisor.setDisable(true);

        detailPanel.getChildren().addAll(lblDetailName, new Separator(), grid, new Separator(),
                btnChangeStanding, btnAssignAdvisor);
    }

    private void displayStudentDetails(Student student) {
        if (student == null) {
            lblDetailName.setText("Select a student");
            lblDetailNumber.setText("-");
            lblDetailEmail.setText("-");
            lblDetailProgram.setText("-");
            lblDetailStatus.setText("-");
            lblDetailGPA.setText("-");
            lblDetailCredits.setText("-");
            lblDetailAdvisor.setText("-");
            btnChangeStanding.setDisable(true);
            btnAssignAdvisor.setDisable(true);
            return;
        }

        lblDetailName.setText(student.getName());
        lblDetailNumber.setText(student.getStudentNumber());
        lblDetailEmail.setText(student.getEmail());
        lblDetailProgram.setText(student.getProgram().getName());
        lblDetailStatus.setText(student.getAcademicStatus().name());
        lblDetailGPA.setText(String.format("%.2f", student.getAcademicRecord().calculateCGPA()));
        lblDetailCredits.setText(String.valueOf(student.getAcademicRecord().getEarnedCredits()));
        lblDetailAdvisor.setText(student.getAdvisor() != null ? student.getAdvisor().getName() : "None Assigned");

        btnChangeStanding.setDisable(false);
        btnAssignAdvisor.setDisable(false);
    }

    private void showChangeStandingDialog() {
        Student selected = studentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Update Academic Standing");
        dialog.setHeaderText("Authorize standing change as Registrar for " + selected.getName());

        ButtonType updateBtnType = new ButtonType("Apply Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        ComboBox<AcademicStaff> registrarCombo = new ComboBox<>();
        List<AcademicStaff> registrars = store.getAcademicStaffList().stream()
                .filter(s -> s.hasRole(StaffRole.REGISTRAR))
                .toList();
        registrarCombo.setItems(FXCollections.observableArrayList(registrars));
        if (!registrars.isEmpty()) registrarCombo.getSelectionModel().selectFirst();
        registrarCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(AcademicStaff object) { return object != null ? object.getName() + " (Registrar)" : ""; }
            @Override public AcademicStaff fromString(String string) { return null; }
        });

        ComboBox<AcademicStatus> statusCombo = new ComboBox<>();
        statusCombo.setItems(FXCollections.observableArrayList(AcademicStatus.values()));
        statusCombo.getSelectionModel().select(selected.getAcademicStatus());

        grid.add(new Label("Authorizing Registrar:"), 0, 0);
        grid.add(registrarCombo, 1, 0);
        grid.add(new Label("New Standing Status:"), 0, 1);
        grid.add(statusCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == updateBtnType) {
                AcademicStaff reg = registrarCombo.getValue();
                AcademicStatus newStatus = statusCombo.getValue();
                if (reg != null && newStatus != null) {
                    selected.updateAcademicStanding(reg, newStatus);
                    store.getNotificationService().notify(selected,
                            "Academic standing was updated to " + newStatus + " by Registrar " + reg.getName());
                    store.notifyStateChanged();
                    return true;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showAssignAdvisorDialog() {
        Student selected = studentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Assign Academic Advisor");
        dialog.setHeaderText("Assign a qualified staff advisor for " + selected.getName());

        ButtonType assignBtnType = new ButtonType("Assign", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(assignBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        ComboBox<AcademicStaff> advisorCombo = new ComboBox<>();
        List<AcademicStaff> advisors = store.getAcademicStaffList().stream()
                .filter(s -> s.hasRole(StaffRole.ADVISOR))
                .toList();
        advisorCombo.setItems(FXCollections.observableArrayList(advisors));
        if (!advisors.isEmpty()) advisorCombo.getSelectionModel().selectFirst();
        advisorCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(AcademicStaff object) { return object != null ? object.getName() + " (Advisor)" : ""; }
            @Override public AcademicStaff fromString(String string) { return null; }
        });

        grid.add(new Label("Select Advisor:"), 0, 0);
        grid.add(advisorCombo, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == assignBtnType) {
                AcademicStaff adv = advisorCombo.getValue();
                if (adv != null) {
                    selected.assignAdvisor(adv);
                    store.getNotificationService().notify(selected, "Assigned new advisor: " + adv.getName());
                    store.notifyStateChanged();
                    return true;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showAddStudentDialog() {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle("Add New Student");
        dialog.setHeaderText("Enter details to enroll a new student");

        ButtonType createBtnType = new ButtonType("Create Student", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField txtId = new TextField("u_" + (store.getStudentRepository().findAll().size() + 1));
        TextField txtNum = new TextField("STU2025" + String.format("%02d", store.getStudentRepository().findAll().size() + 1));
        TextField txtName = new TextField();
        txtName.setPromptText("e.g. John Doe");
        TextField txtEmail = new TextField();
        txtEmail.setPromptText("e.g. john.doe@university.edu");

        ComboBox<Program> programCombo = new ComboBox<>(FXCollections.observableArrayList(store.getPrograms()));
        if (!store.getPrograms().isEmpty()) programCombo.getSelectionModel().selectFirst();
        programCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Program p) { return p != null ? p.getName() : ""; }
            @Override public Program fromString(String s) { return null; }
        });

        grid.add(new Label("User ID:"), 0, 0);
        grid.add(txtId, 1, 0);
        grid.add(new Label("Student Number:"), 0, 1);
        grid.add(txtNum, 1, 1);
        grid.add(new Label("Full Name:"), 0, 2);
        grid.add(txtName, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(txtEmail, 1, 3);
        grid.add(new Label("Degree Program:"), 0, 4);
        grid.add(programCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == createBtnType) {
                if (txtName.getText().isBlank() || txtNum.getText().isBlank()) {
                    return null;
                }
                Student newStudent = new Student(
                        txtId.getText().trim(),
                        txtName.getText().trim(),
                        txtEmail.getText().trim(),
                        LocalDate.of(2004, 1, 1),
                        "555-0000",
                        txtNum.getText().trim(),
                        2025,
                        programCombo.getValue()
                );
                store.addStudent(newStudent);
                return newStudent;
            }
            return null;
        });

        dialog.showAndWait();
    }

    public void refresh() {
        Student currentSelected = studentsTable.getSelectionModel().getSelectedItem();
        studentsMasterList.setAll(store.getStudentRepository().findAll());
        if (currentSelected != null) {
            store.getStudentRepository().findById(currentSelected.getUserId())
                    .ifPresent(s -> {
                        studentsTable.getSelectionModel().select(s);
                        displayStudentDetails(s);
                    });
        }
    }
}
