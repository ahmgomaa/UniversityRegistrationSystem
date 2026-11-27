package ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import model.Enrollment;
import model.Student;
import service.ReportService.SemesterSummary;
import service.ReportService.Transcript;

import java.util.ArrayList;

public class StudentTranscriptController {

    @FXML
    private Label cgpaValue;

    @FXML
    private Label earnedValue;

    @FXML
    private ListView<String> transcriptList;

    public void init(AppContext context, Student student) {
        Transcript transcript = context.getReports().transcript(student);
        ArrayList<String> lines = new ArrayList<>();
        for (SemesterSummary summary : transcript.getSemesters()) {
            lines.add(summary.getSemester() + "  •  GPA "
                    + UiKit.formatGpa(summary.getGpa())
                    + "  •  " + summary.getRegisteredCredits() + " credits");
            for (Enrollment enrollment : summary.getEnrollments()) {
                String grade = enrollment.getGrade() == null
                        ? enrollment.getStatus().toString()
                        : enrollment.getGrade().toString();
                lines.add("    " + enrollment.getCourseSection().getCourse().getCourseCode()
                        + " — " + enrollment.getCourseSection().getCourse().getCourseName()
                        + "  •  " + grade);
            }
        }
        lines.add("CGPA: " + UiKit.formatGpa(transcript.getCgpa())
                + "  •  Earned credits: " + transcript.getEarnedCredits());
        cgpaValue.setText(UiKit.formatGpa(transcript.getCgpa()));
        earnedValue.setText(String.valueOf(transcript.getEarnedCredits()));
        UiKit.prepareList(transcriptList);
        transcriptList.setItems(FXCollections.observableArrayList(lines));
    }
}
