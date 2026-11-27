package ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import model.AcademicStaff;
import model.Student;
import model.enums.AcademicStatus;

public class StaffStatusController {

    @FXML
    private ComboBox<Student> studentBox;

    @FXML
    private ComboBox<AcademicStatus> statusBox;

    @FXML
    private Label statusLabel;

    private AppContext context;
    private AcademicStaff staff;

    public void init(AppContext context, AcademicStaff staff, Student selected) {
        this.context = context;
        this.staff = staff;
        studentBox.setItems(FXCollections.observableArrayList(
                context.getStore().getStudents()));
        if (selected != null) {
            studentBox.setValue(selected);
        }
        statusBox.setItems(FXCollections.observableArrayList(AcademicStatus.values()));
    }

    @FXML
    private void onUpdate() {
        Student student = studentBox.getValue();
        AcademicStatus newStatus = statusBox.getValue();
        if (student == null || newStatus == null) {
            UiKit.showStatus(statusLabel, false, "Select a student and a status first.");
            return;
        }
        try {
            student.updateAcademicStatus(staff, newStatus);
            context.getStore().save();
            String advisorId = student.getAdvisor() == null
                    ? null : student.getAdvisor().getUserId();
            if (SaveCheck.studentSaved(student.getUserId(),
                    newStatus.name(), advisorId)) {
                UiKit.showStatus(statusLabel, true, "Updated status for "
                        + student.getName() + " to " + newStatus + ".");
            } else {
                UiKit.showStatus(statusLabel, false,
                        "The change was not saved. Try again — if it keeps "
                                + "failing, restart the app or contact the registrar.");
            }
        } catch (IllegalArgumentException ex) {
            UiKit.showStatus(statusLabel, false, ex.getMessage());
        }
    }
}
