package ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import model.AcademicStaff;
import model.CourseSection;
import model.Student;
import service.GradingService.RosterEntry;


public class StaffRosterController {

    @FXML
    private ComboBox<CourseSection> sectionBox;

    @FXML
    private RowTable rosterList;

    @FXML
    private Label statusLabel;

    private AppContext context;
    private AcademicStaff staff;

    public void init(AppContext context, AcademicStaff staff) {
        this.context = context;
        this.staff = staff;
        sectionBox.setItems(FXCollections.observableArrayList(
                context.getStore().getSections()));
        rosterList.setSelectable(false);
        rosterList.setColumns(new String[] {"STUDENT", "ID", "GRADE"},
                new double[] {4, 2, 2});
    }

    @FXML
    private void onSectionChosen() {
        CourseSection section = sectionBox.getValue();
        rosterList.clear();
        int count = 0;
        if (section != null) {
            try {
                for (RosterEntry entry : context.getGrading().getRoster(staff, section)) {
                    Student student = entry.getStudent();
                    String grade = entry.getEnrollment().getGrade() == null
                            ? entry.getEnrollment().getStatus().toString()
                            : entry.getEnrollment().getGrade().toString();
                    rosterList.addRow(new String[] {student.getName(),
                            String.valueOf(student.getUserId()), grade});
                    count++;
                }
            } catch (IllegalArgumentException ex) {
                UiKit.showStatus(statusLabel, false, ex.getMessage());
            }
        }
        if (count == 0) {
            rosterList.showEmpty("No students in this section yet.");
        }
    }
}
