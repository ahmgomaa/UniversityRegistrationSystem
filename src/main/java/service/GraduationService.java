package service;

import model.AcademicRecord;
import model.Course;
import model.Program;
import model.Student;
import model.enums.AcademicStatus;
import model.enums.EnrollmentStatus;

import java.util.ArrayList;

public class GraduationService {

    public static class GraduationReport {

        private final boolean eligible;
        private final ArrayList<String> unmetReasons;

        public GraduationReport(boolean eligible, ArrayList<String> unmetReasons) {
            this.eligible = eligible;
            this.unmetReasons = unmetReasons;
        }

        public boolean isEligible() {
            return eligible;
        }

        public ArrayList<String> getUnmetReasons() {
            return unmetReasons;
        }
    }

    public GraduationReport checkGraduationEligibility(Student student) {

        if (student.getAcademicStatus() == AcademicStatus.GRADUATED) {
            return new GraduationReport(true, new ArrayList<String>());
        }

        Program program = student.getProgram();
        AcademicRecord record = student.getAcademicRecord();
        ArrayList<String> reasons = new ArrayList<String>();

        if (record.getEarnedCredits() < program.getTotalCreditsRequired()) {
            reasons.add("Earned credits " + record.getEarnedCredits()
                    + " of " + program.getTotalCreditsRequired() + " required");
        }

        if (record.calculateCGPA() < program.getMinimumCGPA()) {
            reasons.add("CGPA " + round2(record.calculateCGPA())
                    + " is below the minimum " + round2(program.getMinimumCGPA()));
        }

        for (Course c : program.getRequiredCourses()) {
            if (!record.hasPassed(c)) {
                reasons.add("Required course not passed: " + c.getCourseCode()
                        + " " + c.getCourseName());
            }
        }

        int elective = 0;
        for (model.Enrollment e : record.getEnrollments()) {
            if (e.getStatus() == EnrollmentStatus.COMPLETED && e.hasPassed()
                    && program.isElective(e.getCourseSection().getCourse())) {
                elective += e.getCourseSection().getCourse().getCreditHours();
            }
        }
        if (elective < program.getElectiveCredits()) {
            reasons.add("Elective credits " + elective
                    + " of " + program.getElectiveCredits() + " required");
        }

        return new GraduationReport(reasons.isEmpty(), reasons);
    }

    private static String round2(double value) {
        return "" + (Math.round(value * 100.0) / 100.0);
    }
}
