package ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import model.Student;

import java.util.ArrayList;

public class StaffStudentsController {

    @FXML
    private ListView<String> studentsList;

    @FXML
    private Label totalValue;

    @FXML
    private Label activeValue;

    @FXML
    private Label probationValue;

    @FXML
    private Label detailLabel;

    @FXML
    private VBox actionsBox;

    @FXML
    private Label actionsLabel;

    private AppContext context;
    private StaffController parent;

    public void init(AppContext context, StaffController parent) {
        this.context = context;
        this.parent = parent;

        ArrayList<String> lines = new ArrayList<>();
        int active = 0;
        int probation = 0;
        for (Student student : context.getStore().getStudents()) {
            lines.add(describe(student));
            if (student.getAcademicStatus().name().equals("ACTIVE")) {
                active++;
            }
            if (student.getAcademicStatus().name().equals("PROBATION")) {
                probation++;
            }
        }
        totalValue.setText(String.valueOf(context.getStore().getStudents().size()));
        activeValue.setText(String.valueOf(active));
        probationValue.setText(String.valueOf(probation));
        if (lines.isEmpty()) {
            lines.add("No students.");
        }
        UiKit.prepareList(studentsList);
        studentsList.setItems(FXCollections.observableArrayList(lines));

        studentsList.getSelectionModel().selectedIndexProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> obs,
                            Number oldIndex, Number newIndex) {
                        int index = newIndex.intValue();
                        ArrayList<Student> students = context.getStore().getStudents();
                        if (index < 0 || index >= students.size()) {
                            return;
                        }
                        Student selected = students.get(index);
                        parent.setSelectedStudent(selected);
                        detailLabel.setText(describe(selected)
                                + "\n\nAdvisor: "
                                + (selected.getAdvisor() == null
                                        ? "None" : selected.getAdvisor().getName()));
                        actionsLabel.setText(
                                ("Actions for " + selected.getName()).toUpperCase());
                        detailLabel.setVisible(true);
                        detailLabel.setManaged(true);
                        actionsBox.setVisible(true);
                        actionsBox.setManaged(true);
                    }
                });
    }

    @FXML
    private void onStatus() {
        parent.showStatusPane();
    }

    @FXML
    private void onAdvisor() {
        parent.showAdvisorPane();
    }

    private String describe(Student student) {
        String advisor = student.getAdvisor() == null
                ? "No advisor" : student.getAdvisor().getName();
        return student.getName() + " (" + student.getUserId() + ")"
                + "  •  " + student.getAcademicStatus()
                + "  •  CGPA " + UiKit.formatGpa(
                        student.getAcademicRecord().calculateCGPA())
                + "  •  " + student.getProgram().getName()
                + "  •  Advisor: " + advisor;
    }
}
