package ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import model.AcademicStaff;
import model.CourseSection;
import model.Student;
import service.GradingService.RosterEntry;

import java.util.ArrayList;

public class StaffRosterController {

    @FXML
    private ComboBox<CourseSection> sectionBox;

    @FXML
    private ListView<String> rosterList;

    @FXML
    private Label statusLabel;

    private AppContext context;
    private AcademicStaff staff;

    public void init(AppContext context, AcademicStaff staff) {
        this.context = context;
        this.staff = staff;
        sectionBox.setItems(FXCollections.observableArrayList(
                context.getStore().getSections()));
        UiKit.prepareList(rosterList);
    }

    @FXML
    private void onSectionChosen() {
        CourseSection section = sectionBox.getValue();
        ArrayList<String> lines = new ArrayList<>();
        if (section != null) {
            try {
                for (RosterEntry entry : context.getGrading().getRoster(staff, section)) {
                    Student student = entry.getStudent();
                    String grade = entry.getEnrollment().getGrade() == null
                            ? entry.getEnrollment().getStatus().toString()
                            : entry.getEnrollment().getGrade().toString();
                    lines.add(student.getName() + " (" + student.getUserId() + ")"
                            + "  •  " + grade);
                }
            } catch (IllegalArgumentException ex) {
                UiKit.showStatus(statusLabel, false, ex.getMessage());
            }
        }
        if (lines.isEmpty()) {
            lines.add("No students in this section yet.");
        }
        rosterList.setItems(FXCollections.observableArrayList(lines));
    }
}
