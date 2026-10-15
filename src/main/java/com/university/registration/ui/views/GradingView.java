package com.university.registration.ui.views;

import com.university.registration.model.*;
import com.university.registration.model.enums.LetterGrade;
import com.university.registration.ui.context.UniversityDataStore;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Grading desk view allowing instructors to view class rosters
 * and record official grades with automatic letter-grade calculation.
 */
public class GradingView extends VBox {

    private final UniversityDataStore store = UniversityDataStore.getInstance();

    private final ComboBox<Instructor> instructorCombo = new ComboBox<>();
    private final ComboBox<CourseSection> sectionCombo = new ComboBox<>();
    private final TableView<Enrollment> rosterTable = new TableView<>();

    // Grading panel
    private final VBox gradingCard = new VBox(14);
    private final Label lblSelectedStudent = new Label("Select a student from the roster");
    private final Slider scoreSlider = new Slider(0, 100, 85);
    private final TextField scoreField = new TextField("85");
    private final Label lblCalculatedGrade = new Label("A (4.0 pts) - PASSING");
    private final Button btnSubmitGrade = new Button("Submit Official Grade");
    private final Label alertBanner = new Label();

    public GradingView() {
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
        Label title = new Label("Instructor Grading Desk");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Access class rosters and assign final grades to students");
        subtitle.getStyleClass().add("header-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // Feedback banner
        alertBanner.setVisible(false);
        alertBanner.setManaged(false);
        alertBanner.setMaxWidth(Double.MAX_VALUE);
        alertBanner.setWrapText(true);

        // Filter / Selector Card
        HBox selectorsCard = new HBox(16);
        selectorsCard.getStyleClass().add("card");
        selectorsCard.setAlignment(Pos.CENTER_LEFT);

        VBox instBox = new VBox(6);
        Label lblInst = new Label("Instructor:");
        lblInst.getStyleClass().add("card-subtitle");
        instructorCombo.setPrefWidth(280);
        instructorCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Instructor i) { return i != null ? i.getName() + " (" + i.getDepartment().getCode() + ")" : ""; }
            @Override public Instructor fromString(String s) { return null; }
        });
        instructorCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> onInstructorChanged(newV));
        instBox.getChildren().addAll(lblInst, instructorCombo);

        VBox secBox = new VBox(6);
        Label lblSec = new Label("Assigned Section:");
        lblSec.getStyleClass().add("card-subtitle");
        sectionCombo.setPrefWidth(380);
        sectionCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(CourseSection s) {
                return s != null ? s.getSectionId() + " (" + s.getCourse().getCourseCode() + ")" : "";
            }
            @Override public CourseSection fromString(String s) { return null; }
        });
        sectionCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> loadRoster(newV));
        secBox.getChildren().addAll(lblSec, sectionCombo);

        selectorsCard.getChildren().addAll(instBox, secBox);

        // Center split: Roster Table & Grade Input Panel
        setupRosterTable();
        setupGradingCard();

        HBox mainSplit = new HBox(20);
        HBox.setHgrow(rosterTable, Priority.ALWAYS);
        mainSplit.getChildren().addAll(rosterTable, gradingCard);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);

        getChildren().addAll(titleBox, alertBanner, selectorsCard, mainSplit);
    }

    private void setupRosterTable() {
        TableColumn<Enrollment, String> colStudent = new TableColumn<>("STUDENT");
        colStudent.setCellValueFactory(data -> {
            Student s = findStudentForEnrollment(data.getValue());
            return new SimpleStringProperty(s != null ? s.getName() : "Unknown");
        });
        colStudent.setPrefWidth(180);

        TableColumn<Enrollment, String> colNum = new TableColumn<>("STUDENT ID");
        colNum.setCellValueFactory(data -> {
            Student s = findStudentForEnrollment(data.getValue());
            return new SimpleStringProperty(s != null ? s.getStudentNumber() : "-");
        });
        colNum.setPrefWidth(120);

        TableColumn<Enrollment, String> colStatus = new TableColumn<>("ENROLLMENT STATUS");
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        colStatus.setPrefWidth(140);

        TableColumn<Enrollment, String> colGrade = new TableColumn<>("FINAL GRADE");
        colGrade.setCellValueFactory(data -> {
            Grade g = data.getValue().getGrade();
            return new SimpleStringProperty(g != null ? g.getLetterGrade() + " (" + g.getScore() + ")" : "Ungraded");
        });
        colGrade.setPrefWidth(120);

        rosterTable.getColumns().addAll(colStudent, colNum, colStatus, colGrade);

        rosterTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, selected) -> {
            onEnrollmentSelected(selected);
        });
    }

    private void setupGradingCard() {
        gradingCard.getStyleClass().add("card");
        gradingCard.setPrefWidth(340);
        gradingCard.setMinWidth(320);

        Label cardTitle = new Label("Grade Evaluation");
        cardTitle.getStyleClass().add("card-title");

        lblSelectedStudent.getStyleClass().add("card-subtitle");
        lblSelectedStudent.setWrapText(true);

        // Score slider and field synchronization
        scoreSlider.setShowTickLabels(true);
        scoreSlider.setShowTickMarks(true);
        scoreSlider.setMajorTickUnit(25);
        scoreSlider.setBlockIncrement(5);
        scoreSlider.valueProperty().addListener((obs, oldV, newV) -> {
            int score = (int) Math.round(newV.doubleValue());
            scoreField.setText(String.valueOf(score));
            updateGradePreview(score);
        });

        scoreField.setPrefWidth(60);
        scoreField.textProperty().addListener((obs, oldV, newV) -> {
            try {
                int score = Integer.parseInt(newV.trim());
                if (score >= 0 && score <= 100) {
                    scoreSlider.setValue(score);
                    updateGradePreview(score);
                }
            } catch (NumberFormatException ignored) {}
        });

        HBox scoreRow = new HBox(12);
        scoreRow.setAlignment(Pos.CENTER_LEFT);
        Label lblScore = new Label("Numeric Score (0-100):");
        lblScore.getStyleClass().add("card-subtitle");
        scoreRow.getChildren().addAll(lblScore, scoreField);

        lblCalculatedGrade.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34d399;");

        btnSubmitGrade.getStyleClass().add("btn-primary");
        btnSubmitGrade.setMaxWidth(Double.MAX_VALUE);
        btnSubmitGrade.setDisable(true);
        btnSubmitGrade.setOnAction(e -> handleSubmitGrade());

        gradingCard.getChildren().addAll(
                cardTitle,
                lblSelectedStudent,
                new Separator(),
                scoreRow,
                scoreSlider,
                lblCalculatedGrade,
                new Separator(),
                btnSubmitGrade
        );
    }

    private void updateGradePreview(int score) {
        if (score < 0 || score > 100) return;
        LetterGrade letter = LetterGrade.fromScore(score);
        boolean passing = score >= Grade.PASS_MARK;
        lblCalculatedGrade.setText(letter.name() + " (" + letter.getGradePoints() + " pts) - "
                + (passing ? "PASSING" : "FAILING"));
        lblCalculatedGrade.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: "
                + (passing ? "#34d399;" : "#f87171;"));
    }

    private void onInstructorChanged(Instructor instructor) {
        if (instructor == null) {
            sectionCombo.getItems().clear();
            rosterTable.getItems().clear();
            return;
        }

        List<CourseSection> taughtSections = store.getCourseSections().stream()
                .filter(s -> instructor.equals(s.getInstructor()))
                .toList();

        sectionCombo.setItems(FXCollections.observableArrayList(taughtSections));
        if (!taughtSections.isEmpty()) {
            sectionCombo.getSelectionModel().selectFirst();
        } else {
            rosterTable.getItems().clear();
        }
    }

    private void loadRoster(CourseSection section) {
        if (section == null) {
            rosterTable.getItems().clear();
            return;
        }
        rosterTable.setItems(FXCollections.observableArrayList(section.getEnrollments()));
    }

    private void onEnrollmentSelected(Enrollment enrollment) {
        if (enrollment == null) {
            lblSelectedStudent.setText("Select a student from the roster");
            btnSubmitGrade.setDisable(true);
            return;
        }

        Student s = findStudentForEnrollment(enrollment);
        if (s != null) {
            lblSelectedStudent.setText("Grading: " + s.getName() + " (" + s.getStudentNumber() + ")");
        } else {
            lblSelectedStudent.setText("Grading Selected Student");
        }

        if (enrollment.getGrade() != null) {
            scoreSlider.setValue(enrollment.getGrade().getScore());
            btnSubmitGrade.setText("Update Grade");
        } else {
            scoreSlider.setValue(85);
            btnSubmitGrade.setText("Submit Official Grade");
        }

        btnSubmitGrade.setDisable(false);
    }

    private void handleSubmitGrade() {
        Instructor instructor = instructorCombo.getValue();
        Enrollment enrollment = rosterTable.getSelectionModel().getSelectedItem();
        if (instructor == null || enrollment == null) return;

        Student student = findStudentForEnrollment(enrollment);
        if (student == null) {
            showAlert("Error: Student matching enrollment could not be found.", true);
            return;
        }

        try {
            int score = (int) Math.round(scoreSlider.getValue());
            store.getGradingService().recordGrade(instructor, student, enrollment, score);
            showAlert("Grade recorded successfully for " + student.getName() + ": "
                    + enrollment.getGrade().getLetterGrade() + " (" + score + ")", false);
            store.notifyStateChanged();
        } catch (Exception ex) {
            showAlert("Grading Error: " + ex.getMessage(), true);
        }
    }

    private Student findStudentForEnrollment(Enrollment enrollment) {
        for (Student s : store.getStudentRepository().findAll()) {
            if (s.getAcademicRecord().getEnrollments().contains(enrollment)) {
                return s;
            }
        }
        return null;
    }

    private void showAlert(String message, boolean isError) {
        alertBanner.setText(message);
        alertBanner.getStyleClass().removeAll("alert-box", "alert-success", "alert-error");
        alertBanner.getStyleClass().addAll("alert-box", isError ? "alert-error" : "alert-success");
        alertBanner.setVisible(true);
        alertBanner.setManaged(true);
    }

    public void refresh() {
        Instructor prevInst = instructorCombo.getValue();
        CourseSection prevSec = sectionCombo.getValue();

        instructorCombo.setItems(FXCollections.observableArrayList(store.getInstructors()));
        if (prevInst != null) {
            store.getInstructors().stream()
                    .filter(i -> i.getUserId().equals(prevInst.getUserId()))
                    .findFirst()
                    .ifPresent(instructorCombo.getSelectionModel()::select);
        } else if (!instructorCombo.getItems().isEmpty()) {
            instructorCombo.getSelectionModel().selectFirst();
        }

        if (prevSec != null) {
            sectionCombo.getItems().stream()
                    .filter(s -> s.getSectionId().equals(prevSec.getSectionId()))
                    .findFirst()
                    .ifPresent(sectionCombo.getSelectionModel()::select);
        }
    }
}
