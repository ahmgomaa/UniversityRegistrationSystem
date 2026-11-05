package com.university.registration.service;

import com.university.registration.dto.GraduationReport;
import com.university.registration.model.AcademicRecord;
import com.university.registration.model.Course;
import com.university.registration.model.Program;
import com.university.registration.model.Student;
import com.university.registration.model.enums.AcademicStatus;
import com.university.registration.model.enums.EnrollmentStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks whether a student has satisfied every requirement of their program.
 */
public class GraduationService {

    public GraduationReport checkGraduationEligibility(Student student) {
        if (student.getAcademicStatus() == AcademicStatus.GRADUATED) {
            return new GraduationReport(true, List.of());
        }

        Program program = student.getProgram();
        AcademicRecord record = student.getAcademicRecord();
        List<String> reasons = new ArrayList<>();

        int earned = record.getEarnedCredits();
        if (earned < program.getTotalCreditsRequired()) {
            reasons.add("Earned credits %d of %d required".formatted(earned, program.getTotalCreditsRequired()));
        }

        double cgpa = record.calculateCGPA();
        if (cgpa < program.getMinimumCGPA()) {
            reasons.add("CGPA %.2f is below the minimum %.2f".formatted(cgpa, program.getMinimumCGPA()));
        }

        for (Course required : program.getRequiredCourses()) {
            if (!record.hasPassed(required)) {
                reasons.add("Required course not passed: " + required.getCourseCode()
                        + " " + required.getCourseName());
            }
        }

        int electiveEarned = record.getEnrollments().stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED && e.hasPassed())
                .map(e -> e.getCourseSection().getCourse())
                .filter(program::isElective)
                .mapToInt(Course::getCreditHours)
                .sum();
        if (electiveEarned < program.getElectiveCredits()) {
            reasons.add("Elective credits %d of %d required".formatted(electiveEarned, program.getElectiveCredits()));
        }

        return new GraduationReport(reasons.isEmpty(), reasons);
    }
}
