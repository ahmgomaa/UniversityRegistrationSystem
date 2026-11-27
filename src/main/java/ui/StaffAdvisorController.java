package ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import model.AcademicStaff;
import model.Student;
import model.enums.StaffRole;

import java.util.ArrayList;

public class StaffAdvisorController {

    @FXML
    private ComboBox<Student> studentBox;

    @FXML
    private ComboBox<AcademicStaff> advisorBox;

    @FXML
    private Label statusLabel;

    private AppContext context;

    public void init(AppContext context, Student selected) {
        this.context = context;
        studentBox.setItems(FXCollections.observableArrayList(
                context.getStore().getStudents()));
        if (selected != null) {
            studentBox.setValue(selected);
        }
        ArrayList<AcademicStaff> advisors = new ArrayList<>();
        for (AcademicStaff member : context.getStore().getStaff()) {
            if (member.hasRole(StaffRole.ADVISOR)) {
                advisors.add(member);
            }
        }
        advisorBox.setItems(FXCollections.observableArrayList(advisors));
    }

    @FXML
    private void onAssign() {
        Student student = studentBox.getValue();
        AcademicStaff advisor = advisorBox.getValue();
        if (student == null || advisor == null) {
            UiKit.showStatus(statusLabel, false, "Select a student and an advisor first.");
            return;
        }
        try {
            student.assignAdvisor(advisor);
            context.getStore().save();
            if (SaveCheck.studentSaved(student.getUserId(),
                    student.getAcademicStatus().name(), advisor.getUserId())) {
                UiKit.showStatus(statusLabel, true, "Assigned " + advisor.getName()
                        + " to " + student.getName() + ".");
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
