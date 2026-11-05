package com.university.registration.model;

import com.university.registration.model.enums.EnrollmentStatus;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A student's enrollment in one course section. Progresses
 * ENROLLED -&gt; WITHDRAWN or ENROLLED -&gt; COMPLETED.
 */
public class Enrollment {

    private final CourseSection courseSection;
    private final LocalDate enrolledOn;
    private EnrollmentStatus status;
    private Grade grade;
    private LocalDate withdrawnOn;

    public Enrollment(CourseSection courseSection, LocalDate enrolledOn) {
        this.courseSection = Objects.requireNonNull(courseSection, "courseSection");
        this.enrolledOn = Objects.requireNonNull(enrolledOn, "enrolledOn");
        this.status = EnrollmentStatus.ENROLLED;
    }

    public CourseSection getCourseSection() {
        return courseSection;
    }

    public LocalDate getEnrolledOn() {
        return enrolledOn;
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

    /** Restores a persisted state (used by repositories when rehydrating). */
    public void restoreState(EnrollmentStatus status, Grade grade, LocalDate withdrawnOn) {
        this.status = Objects.requireNonNull(status, "status");
        this.grade = grade;
        this.withdrawnOn = withdrawnOn;
    }

    public void withdraw(LocalDate date) {
        if (status != EnrollmentStatus.ENROLLED) {
            throw new IllegalStateException("Only an active enrollment can be withdrawn, was " + status);
        }
        this.status = EnrollmentStatus.WITHDRAWN;
        this.withdrawnOn = date;
    }

    public void complete(Grade grade) {
        if (status != EnrollmentStatus.ENROLLED) {
            throw new IllegalStateException("Only an active enrollment can be completed, was " + status);
        }
        this.grade = Objects.requireNonNull(grade, "grade");
        this.status = EnrollmentStatus.COMPLETED;
    }

    public boolean hasPassed() {
        return status == EnrollmentStatus.COMPLETED && grade != null && grade.isPassed();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Enrollment that)) return false;
        return Objects.equals(courseSection, that.courseSection);
    }

    @Override
    public int hashCode() {
        return courseSection.hashCode();
    }

    @Override
    public String toString() {
        return "%s %s%s".formatted(courseSection, status,
                grade != null ? " " + grade.getLetterGrade().display() : "");
    }
}
