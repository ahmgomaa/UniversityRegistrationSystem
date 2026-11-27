package ui;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import model.Enrollment;
import model.Student;
import service.ReportService.SemesterSummary;
import service.ReportService.Transcript;

public class StudentTranscriptController {

    @FXML
    private Label cgpaValue;

    @FXML
    private Label earnedValue;

    @FXML
    private ScrollPane transcriptListScroll;

    private RowTable transcriptList;

    public void init(AppContext context, Student student) {
        transcriptList = new RowTable(transcriptListScroll);
        Transcript transcript = context.getReports().transcript(student);
        transcriptList.setSelectable(false);
        transcriptList.setColumns(new String[] {"CODE", "COURSE", "GRADE"},
                new double[] {2, 7, 2});
        for (SemesterSummary summary : transcript.getSemesters()) {
            transcriptList.addGroup(summary.getSemester() + "  •  GPA "
                    + UiKit.formatGpa(summary.getGpa())
                    + "  •  " + summary.getRegisteredCredits() + " credits");
            for (Enrollment enrollment : summary.getEnrollments()) {
                String grade = enrollment.getGrade() == null
                        ? enrollment.getStatus().toString()
                        : enrollment.getGrade().toString();
                transcriptList.addRow(new String[] {
                        enrollment.getCourseSection().getCourse().getCourseCode(),
                        enrollment.getCourseSection().getCourse().getCourseName(),
                        grade});
            }
        }
        cgpaValue.setText(UiKit.formatGpa(transcript.getCgpa()));
        earnedValue.setText(String.valueOf(transcript.getEarnedCredits()));
    }
}
