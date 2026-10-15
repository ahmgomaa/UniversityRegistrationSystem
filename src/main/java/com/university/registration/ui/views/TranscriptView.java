package com.university.registration.ui.views;

import com.university.registration.dto.Transcript;
import com.university.registration.dto.TranscriptEntry;
import com.university.registration.model.Student;
import com.university.registration.ui.context.UniversityDataStore;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Academic Records and Official Transcript view.
 * Computes and renders student transcripts, credit totals, and cumulative GPAs.
 */
public class TranscriptView extends VBox {

    private final UniversityDataStore store = UniversityDataStore.getInstance();

    private final ComboBox<Student> studentCombo = new ComboBox<>();
    private final Label lblGPA = new Label("0.00");
    private final Label lblCredits = new Label("0");
    private final Label lblStatus = new Label("-");
    private final TableView<TranscriptEntry> transcriptTable = new TableView<>();
    private final TextArea textTranscriptArea = new TextArea();

    public TranscriptView() {
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
        Label title = new Label("Academic Transcript & Records");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Generate official transcripts, quality points, and cumulative grade summaries");
        subtitle.getStyleClass().add("header-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // Selector Bar
        HBox selectorCard = new HBox(16);
        selectorCard.getStyleClass().add("card");
        selectorCard.setAlignment(Pos.CENTER_LEFT);

        Label lblSelect = new Label("Select Student:");
        lblSelect.getStyleClass().add("card-subtitle");

        studentCombo.setPrefWidth(320);
        studentCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Student s) { return s != null ? s.getStudentNumber() + " - " + s.getName() : ""; }
            @Override public Student fromString(String s) { return null; }
        });
        studentCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldV, selected) -> loadTranscript(selected));

        selectorCard.getChildren().addAll(lblSelect, studentCombo);

        // Academic Summary Stat Cards
        HBox summaryRow = new HBox(16);
        summaryRow.setAlignment(Pos.CENTER_LEFT);

        VBox cardGPA = createStatCard("Cumulative GPA (CGPA)", lblGPA);
        VBox cardCredits = createStatCard("Total Earned Credits", lblCredits);
        VBox cardStatus = createStatCard("Academic Standing", lblStatus);

        HBox.setHgrow(cardGPA, Priority.ALWAYS);
        HBox.setHgrow(cardCredits, Priority.ALWAYS);
        HBox.setHgrow(cardStatus, Priority.ALWAYS);
        summaryRow.getChildren().addAll(cardGPA, cardCredits, cardStatus);

        // Transcript Content: Table View & Plaintext Transcript Tabs
        setupTranscriptTable();
        textTranscriptArea.setEditable(false);
        textTranscriptArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 13px;");

        TabPane tabs = new TabPane();
        Tab tabTable = new Tab("Course Records Table", transcriptTable);
        tabTable.setClosable(false);
        Tab tabText = new Tab("Official Formatted Transcript", textTranscriptArea);
        tabText.setClosable(false);
        tabs.getTabs().addAll(tabTable, tabText);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        getChildren().addAll(titleBox, selectorCard, summaryRow, tabs);
    }

    private VBox createStatCard(String labelText, Label valueLabel) {
        VBox card = new VBox(6);
        card.getStyleClass().add("stat-card");
        valueLabel.getStyleClass().add("stat-value");
        Label label = new Label(labelText);
        label.getStyleClass().add("stat-label");
        card.getChildren().addAll(valueLabel, label);
        return card;
    }

    private void setupTranscriptTable() {
        TableColumn<TranscriptEntry, String> colCode = new TableColumn<>("COURSE CODE");
        colCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourseCode()));
        colCode.setPrefWidth(140);

        TableColumn<TranscriptEntry, String> colName = new TableColumn<>("COURSE TITLE");
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourseName()));
        colName.setPrefWidth(280);

        TableColumn<TranscriptEntry, String> colCredits = new TableColumn<>("CREDITS");
        colCredits.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCredits())));
        colCredits.setPrefWidth(100);

        TableColumn<TranscriptEntry, String> colGrade = new TableColumn<>("LETTER GRADE");
        colGrade.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGrade().name()));
        colGrade.setPrefWidth(130);
        colGrade.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    if (item.startsWith("A")) {
                        badge.getStyleClass().add("badge-success");
                    } else if (item.startsWith("B")) {
                        badge.getStyleClass().add("badge-info");
                    } else if (item.startsWith("C") || item.startsWith("D")) {
                        badge.getStyleClass().add("badge-warning");
                    } else {
                        badge.getStyleClass().add("badge-danger");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TableColumn<TranscriptEntry, String> colPoints = new TableColumn<>("GRADE POINTS");
        colPoints.setCellValueFactory(data -> new SimpleStringProperty(
                String.format("%.2f", data.getValue().getGrade().getGradePoints())));
        colPoints.setPrefWidth(120);

        transcriptTable.getColumns().addAll(colCode, colName, colCredits, colGrade, colPoints);
    }

    private void loadTranscript(Student student) {
        if (student == null) {
            lblGPA.setText("0.00");
            lblCredits.setText("0");
            lblStatus.setText("-");
            transcriptTable.getItems().clear();
            textTranscriptArea.clear();
            return;
        }

        Transcript transcript = store.getReportService().transcriptReport(student);
        lblGPA.setText(String.format("%.2f", transcript.getCumulativeGPA()));
        lblCredits.setText(String.valueOf(transcript.getTotalCredits()));
        lblStatus.setText(student.getAcademicStatus().name());

        transcriptTable.setItems(FXCollections.observableArrayList(transcript.getRecords()));

        // Build formatted ASCII transcript
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("                          OFFICIAL ACADEMIC TRANSCRIPT                                 \n");
        sb.append("========================================================================================\n\n");
        sb.append(String.format(" Student Name    : %-30s Student ID : %s\n", transcript.getStudentName(), transcript.getStudentNumber()));
        sb.append(String.format(" Degree Program  : %-30s Academic Standing : %s\n", student.getProgram().getName(), student.getAcademicStatus()));
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format(" %-12s | %-36s | %-8s | %-8s | %-8s\n", "COURSE CODE", "COURSE TITLE", "CREDITS", "GRADE", "POINTS"));
        sb.append("----------------------------------------------------------------------------------------\n");

        if (transcript.getRecords().isEmpty()) {
            sb.append(" No completed courses recorded.\n");
        } else {
            for (TranscriptEntry entry : transcript.getRecords()) {
                sb.append(String.format(" %-12s | %-36s | %-8d | %-8s | %-8.2f\n",
                        entry.getCourseCode(),
                        entry.getCourseName(),
                        entry.getCredits(),
                        entry.getGrade().name(),
                        entry.getGrade().getGradePoints()));
            }
        }

        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format(" TOTAL EARNED CREDITS : %-10d | CUMULATIVE GPA (CGPA) : %.2f\n",
                transcript.getTotalCredits(), transcript.getCumulativeGPA()));
        sb.append("========================================================================================\n");

        textTranscriptArea.setText(sb.toString());
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
