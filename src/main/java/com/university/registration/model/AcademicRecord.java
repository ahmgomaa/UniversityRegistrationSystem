package com.university.registration.model;

import com.university.registration.model.enums.EnrollmentStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a student's complete academic record across all semesters.
 * Tracks enrollments and computes cumulative GPA and earned credits.
 */
public class AcademicRecord {

    private final List<Enrollment> enrollments;

    public AcademicRecord() {
        this.enrollments = new ArrayList<>();
    }

    public List<Enrollment> getEnrollments() {
        return Collections.unmodifiableList(enrollments);
    }

    /**
     * Adds an enrollment to this academic record.
     *
     * @param enrollment the enrollment to add
     */
    public void addEnrollment(Enrollment enrollment) {
        Objects.requireNonNull(enrollment, "enrollment must not be null");
        enrollments.add(enrollment);
    }

    /**
     * Returns all enrollments for the given semester.
     *
     * @param semester the semester to filter by
     * @return list of enrollments in the specified semester
     */
    public List<Enrollment> getEnrollments(Semester semester) {
        return enrollments.stream()
                .filter(e -> e.getSection().getSemester().equals(semester))
                .collect(Collectors.toList());
    }

    /**
     * Checks if the student has previously enrolled in the given course during the given semester.
     *
     * @param course   the course to check
     * @param semester the semester to check
     * @return true if the student has enrolled in this course during this semester
     */
    public boolean hasEnrolledBefore(Course course, Semester semester) {
        return enrollments.stream()
                .anyMatch(e -> e.getSection().getCourse().equals(course)
                        && e.getSection().getSemester().equals(semester));
    }

    /**
     * Checks if the student has completed the given course with a passing grade.
     *
     * @param course the course to check
     * @return true if the course has been completed with a passing grade
     */
    public boolean hasCompletedCourse(Course course) {
        return enrollments.stream()
                .anyMatch(e -> e.getSection().getCourse().equals(course)
                        && e.getStatus() == EnrollmentStatus.COMPLETED
                        && e.getGrade() != null
                        && e.getGrade().isPassing());
    }

    /**
     * Returns the total number of earned credits from completed courses.
     *
     * @return the total earned credits
     */
    public int getEarnedCredits() {
        return enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED
                        && e.getGrade() != null
                        && e.getGrade().isPassing())
                .mapToInt(e -> e.getSection().getCourse().getCredits())
                .sum();
    }

    /**
     * Checks if the student is currently enrolled in any course during the given semester.
     *
     * @param semester the semester to check
     * @return true if the student has at least one active enrollment in the semester
     */
    public boolean isCurrentlyEnrolled(Semester semester) {
        return enrollments.stream()
                .anyMatch(e -> e.getSection().getSemester().equals(semester) && e.isActive());
    }

    /**
     * Calculates the cumulative GPA across all completed enrollments.
     *
     * @return the cumulative GPA, or 0.0 if no grades recorded
     */
    public double calculateCGPA() {
        List<Enrollment> completedEnrollments = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED && e.getGrade() != null)
                .collect(Collectors.toList());

        if (completedEnrollments.isEmpty()) {
            return 0.0;
        }

        double totalQualityPoints = 0.0;
        int totalCredits = 0;

        for (Enrollment enrollment : completedEnrollments) {
            int credits = enrollment.getSection().getCourse().getCredits();
            double gradePoints = enrollment.getGrade().getGradePoints();
            totalQualityPoints += credits * gradePoints;
            totalCredits += credits;
        }

        return totalCredits > 0 ? totalQualityPoints / totalCredits : 0.0;
    }
}
