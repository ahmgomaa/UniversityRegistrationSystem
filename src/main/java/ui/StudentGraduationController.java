package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import model.Student;
import service.GraduationService.GraduationReport;

import java.util.ArrayList;

public class StudentGraduationController {

    @FXML
    private Label line1;

    @FXML
    private Label line2;

    @FXML
    private Label line3;

    @FXML
    private Label creditsLabel;

    @FXML
    private ProgressBar creditsProgress;

    public void init(Student student, AppContext context) {
        GraduationReport report = context.getGraduation().checkGraduationEligibility(student);
        int earnedCredits = student.getAcademicRecord().getEarnedCredits();
        int requiredCredits = student.getProgram().getTotalCreditsRequired();
        if (report.isEligible()) {
            line1.setText("You are eligible to graduate.");
            line2.setText("CGPA: " + UiKit.formatGpa(
                    student.getAcademicRecord().calculateCGPA())
                    + " (minimum " + student.getProgram().getMinimumCGPA() + ")");
            line3.setText("Earned credits: " + earnedCredits + " / " + requiredCredits);
        } else {
            ArrayList<String> items = new ArrayList<>();
            items.add("Not eligible yet:");
            items.addAll(report.getUnmetReasons());
            line1.setText(String.join("\n• ", items));
            line2.setVisible(false);
            line2.setManaged(false);
            line3.setVisible(false);
            line3.setManaged(false);
        }
        creditsLabel.setText(earnedCredits + " of " + requiredCredits + " credits");
        creditsProgress.setProgress(requiredCredits == 0 ? 0
                : (double) earnedCredits / requiredCredits);
        creditsProgress.getStyleClass().add(
                report.isEligible() ? "green-progress" : "orange-progress");
    }
}
