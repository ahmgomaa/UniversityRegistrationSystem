package com.university.registration.service;

import com.university.registration.dto.GraduationReport;
import com.university.registration.model.AcademicRecord;
import com.university.registration.model.Course;
import com.university.registration.model.Program;
import com.university.registration.model.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service responsible for evaluating a student's graduation eligibility.
 */
public class GraduationService {

    /**
     * Checks whether the student is eligible to graduate based on their program
     * requirements.
     * <p>
     * Validates:
     * <ul>
     * <li>All required courses have been completed with passing grades</li>
     * <li>Total earned credits meet the program's credit requirement</li>
     * </ul>
     *
     * @param student the student to evaluate
     * @return a GraduationReport with eligibility status and any unfulfilled
     *         reasons
     */
    public GraduationReport checkGraduationEligibility(Student student) {
        Objects.requireNonNull(student, "student must not be null");

        Program program = student.getProgram();
        AcademicRecord record = student.getAcademicRecord();
        List<String> unfulfilledReasons = new ArrayList<>();
        List<String> completedCourses = new ArrayList<>();

        // Check required courses
        for (Course required : program.getRequiredCourses()) {
            if (record.hasCompletedCourse(required)) {
                completedCourses.add(required.getCourseCode());
            } else {
                unfulfilledReasons.add("Missing required course: " + required.getCourseCode());
            }
        }

        // Add any completed elective courses
        for (Course elective : program.getElectiveCourses()) {
            if (record.hasCompletedCourse(elective)) {
                completedCourses.add(elective.getCourseCode());
            }
        }

        // Check total credits
        int earnedCredits = record.getEarnedCredits();
        if (earnedCredits < program.getTotalCreditsRequired()) {
            unfulfilledReasons.add("Insufficient credits: earned "
                    + earnedCredits + " of " + program.getTotalCreditsRequired() + " required");
        }

        boolean eligible = unfulfilledReasons.isEmpty();
        return new GraduationReport(eligible, completedCourses, unfulfilledReasons);
    }
}
