package com.university.registration.model;

import com.university.registration.model.enums.EnrollmentStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The complete enrollment history of one student. Owned by {@link Student}
 * (composition). All GPA/credit computations flow through this class.
 */
public class AcademicRecord {

    private final List<Enrollment> enrollments = new ArrayList<>();

    public void addEnrollment(Enrollment e) {
        if (e == null || contains(e)) {
            throw new IllegalArgumentException("Enrollment is null or already stored");
        }
        enrollments.add(e);
    }

    public boolean contains(Enrollment e) {
        return enrollments.stream().anyMatch(existing -> existing.equals(e));
    }

    public List<Enrollment> getEnrollments() {
        return Collections.unmodifiableList(enrollments);
    }

    /** All enrollments (any status) belonging to the given semester. */
    public List<Enrollment> getEnrollments(Semester semester) {
        return enrollments.stream()
                .filter(e -> e.getCourseSection().getSemester().equals(semester))
                .toList();
    }

    /** Whether the student ever enrolled in this course in this semester. */
    public boolean hasEnrolledIn(Course course, Semester semester) {
        return enrollments.stream().anyMatch(e ->
                e.getCourseSection().getCourse().equals(course)
                        && e.getCourseSection().getSemester().equals(semester));
    }

    /** Whether the student completed the course with a passing grade (any semester). */
    public boolean hasPassed(Course course) {
        return enrollments.stream().anyMatch(e ->
                e.getCourseSection().getCourse().equals(course) && e.hasPassed());
    }

    /** Credits the student is currently registered for in the semester (status ENROLLED). */
    public int getRegisteredCredits(Semester semester) {
        return enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ENROLLED)
                .filter(e -> e.getCourseSection().getSemester().equals(semester))
                .mapToInt(e -> e.getCourseSection().getCourse().getCreditHours())
                .sum();
    }

    /** Total credits earned from passed, completed courses. */
    public int getEarnedCredits() {
        return enrollments.stream()
                .filter(Enrollment::hasPassed)
                .mapToInt(e -> e.getCourseSection().getCourse().getCreditHours())
                .sum();
    }

    /** GPA for one semester over completed graded enrollments; 0.0 when none. */
    public double calculateGPA(Semester semester) {
        List<Enrollment> graded = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED && e.getGrade() != null)
                .filter(e -> e.getCourseSection().getSemester().equals(semester))
                .toList();
        return weightedGpa(graded);
    }

    /** Cumulative GPA over all completed graded enrollments; 0.0 when none. */
    public double calculateCGPA() {
        List<Enrollment> graded = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED && e.getGrade() != null)
                .toList();
        return weightedGpa(graded);
    }

    private double weightedGpa(List<Enrollment> graded) {
        int credits = 0;
        double points = 0;
        for (Enrollment e : graded) {
            int ch = e.getCourseSection().getCourse().getCreditHours();
            credits += ch;
            points += e.getGrade().getGradePoints() * ch;
        }
        return credits == 0 ? 0.0 : points / credits;
    }

    /** Earliest semester in which the student enrolled (for records with no grades). */
    public Optional<Semester> firstSemester() {
        return enrollments.stream()
                .map(e -> e.getCourseSection().getSemester())
                .distinct()
                .min(Semester::compareTo);
    }

    @Override
    public String toString() {
        return "AcademicRecord{enrollments=%d, earnedCredits=%d}".formatted(enrollments.size(), getEarnedCredits());
    }
}
