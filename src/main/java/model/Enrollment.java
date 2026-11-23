package model;

import model.enums.EnrollmentStatus;

import java.time.LocalDate;

public class Enrollment {

    private final CourseSection courseSection;
    private final LocalDate enrolledOn;
    private EnrollmentStatus status;
    private Grade grade;
    private LocalDate withdrawnOn;

    public Enrollment(CourseSection courseSection, LocalDate enrolledOn) {
        if (courseSection == null || enrolledOn == null) {
            throw new IllegalArgumentException("Section and enrollment date are required");
        }
        this.courseSection = courseSection;
        this.enrolledOn = enrolledOn;
        this.status = EnrollmentStatus.ENROLLED;
    }

    public void restoreState(EnrollmentStatus status, Grade grade, LocalDate withdrawnOn) {
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null");
        }
        this.status = status;
        this.grade = grade;
        this.withdrawnOn = withdrawnOn;
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
        if (grade == null) {
            throw new IllegalArgumentException("Grade must not be null");
        }
        this.grade = grade;
        this.status = EnrollmentStatus.COMPLETED;
    }

    public boolean hasPassed() {
        return status == EnrollmentStatus.COMPLETED
                && grade != null
                && grade.isPassed();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Enrollment) {
            Enrollment other = (Enrollment) obj;
            return this.courseSection.equals(other.courseSection);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return courseSection.hashCode();
    }
}
