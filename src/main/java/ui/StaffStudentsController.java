package ui;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import model.Student;

import java.util.ArrayList;

public class StaffStudentsController {

    @FXML
    private ScrollPane studentsListScroll;

    private RowTable studentsList;

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
        studentsList = new RowTable(studentsListScroll);
        this.context = context;
        this.parent = parent;

        studentsList.setColumns(
                new String[] {"STUDENT", "ID", "STATUS", "CGPA", "PROGRAM", "ADVISOR"},
                new double[] {3, 2, 3, 2, 4, 3});
        int active = 0;
        int probation = 0;
        for (Student student : context.getStore().getStudents()) {
            String advisor = student.getAdvisor() == null
                    ? "None" : student.getAdvisor().getName();
            studentsList.addRow(new String[] {student.getName(),
                    String.valueOf(student.getUserId()),
                    student.getAcademicStatus().name(),
                    UiKit.formatGpa(student.getAcademicRecord().calculateCGPA()),
                    student.getProgram().getName(), advisor});
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
        if (context.getStore().getStudents().isEmpty()) {
            studentsList.showEmpty("No students.");
        }

        studentsList.setSelectionListener(new RowTable.SelectionListener() {
            @Override
            public void selected(int index) {
                ArrayList<Student> students = context.getStore().getStudents();
                if (index < 0 || index >= students.size()) {
                    return;
                }
                Student selected = students.get(index);
                parent.setSelectedStudent(selected);
                detailLabel.setText(describe(selected));
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
