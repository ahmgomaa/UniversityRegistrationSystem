package com.university.registration.model;

import com.university.registration.model.enums.EnrollmentStatus;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a student's enrollment in a specific course section.
 * Tracks enrollment status, grade, and withdrawal date.
 */
public class Enrollment {

    private final CourseSection section;
    private EnrollmentStatus status;
    private Grade grade;
    private LocalDate withdrawnOn;

    public Enrollment(CourseSection section) {
        this.section = Objects.requireNonNull(section, "section must not be null");
        this.status = EnrollmentStatus.ENROLLED;
    }

    public CourseSection getSection() {
        return section;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public Grade getGrade() {
        return grade;
    }

    public LocalDate getWithdrawnOn() {
        return withdrawnOn;
    }

    /**
     * Checks if this enrollment is currently active (status is ENROLLED).
     *
     * @return true if the student is actively enrolled
     */
    public boolean isActive() {
        return status == EnrollmentStatus.ENROLLED;
    }

    /**
     * Assigns a grade and marks the enrollment as COMPLETED.
     *
     * @param grade the grade to assign
     * @throws IllegalStateException if the enrollment is not in ENROLLED status
     */
    public void completeGrade(Grade grade) {
        if (status != EnrollmentStatus.ENROLLED) {
            throw new IllegalStateException(
                    "Cannot assign grade to enrollment with status: " + status);
        }
        Objects.requireNonNull(grade, "grade must not be null");
        this.grade = grade;
        this.status = EnrollmentStatus.COMPLETED;
    }

    /**
     * Withdraws from this enrollment, recording the withdrawal date.
     *
     * @param date the date of withdrawal
     * @throws IllegalStateException if the enrollment is not in ENROLLED status
     */
    public void cancelRegistration(LocalDate date) {
        if (status != EnrollmentStatus.ENROLLED) {
            throw new IllegalStateException(
                    "Cannot withdraw from enrollment with status: " + status);
        }
        this.status = EnrollmentStatus.WITHDRAWN;
        this.withdrawnOn = Objects.requireNonNull(date, "date must not be null");
    }

    @Override
    public String toString() {
        return "Enrollment{section=" + section.getSectionId()
                + ", status=" + status
                + (grade != null ? ", grade=" + grade : "")
                + "}";
    }
}
