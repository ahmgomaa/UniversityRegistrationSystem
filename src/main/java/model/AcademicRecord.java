package model;

import model.enums.EnrollmentStatus;

import java.util.ArrayList;

public class AcademicRecord {

    private final ArrayList<Enrollment> enrollments;

    public AcademicRecord() {
        this.enrollments = new ArrayList<Enrollment>();
    }

    public void addEnrollment(Enrollment e) {
        if (e == null || contains(e)) {
            throw new IllegalArgumentException("Enrollment is null or already stored");
        }
        enrollments.add(e);
    }

    public boolean contains(Enrollment e) {
        for (Enrollment existing : enrollments) {
            if (existing.equals(e)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Enrollment> getEnrollments() {
        return enrollments;
    }

    public ArrayList<Enrollment> getEnrollmentsInSemester(Semester semester) {
        ArrayList<Enrollment> result = new ArrayList<Enrollment>();
        for (Enrollment e : enrollments) {
            if (e.getCourseSection().getSemester().equals(semester)) {
                result.add(e);
            }
        }
        return result;
    }

    public boolean hasEnrolledIn(Course course, Semester semester) {
        for (Enrollment e : enrollments) {
            Course sectionCourse = e.getCourseSection().getCourse();
            Semester sectionSemester = e.getCourseSection().getSemester();
            if (sectionCourse.equals(course) && sectionSemester.equals(semester)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPassed(Course course) {
        for (Enrollment e : enrollments) {
            if (e.getCourseSection().getCourse().equals(course) && e.hasPassed()) {
                return true;
            }
        }
        return false;
    }

    public int getRegisteredCredits(Semester semester) {
        int credits = 0;
        for (Enrollment e : enrollments) {
            if (e.getStatus() == EnrollmentStatus.ENROLLED
                    && e.getCourseSection().getSemester().equals(semester)) {
                credits += e.getCourseSection().getCourse().getCreditHours();
            }
        }
        return credits;
    }

    public int getEarnedCredits() {
        int credits = 0;
        for (Enrollment e : enrollments) {
            if (e.hasPassed()) {
                credits += e.getCourseSection().getCourse().getCreditHours();
            }
        }
        return credits;
    }

    public double calculateGPA(Semester semester) {
        ArrayList<Enrollment> graded = new ArrayList<Enrollment>();
        for (Enrollment e : enrollments) {
            if (e.getStatus() == EnrollmentStatus.COMPLETED && e.getGrade() != null
                    && e.getCourseSection().getSemester().equals(semester)) {
                graded.add(e);
            }
        }
        return weightedGpa(graded);
    }

    public double calculateCGPA() {
        ArrayList<Enrollment> graded = new ArrayList<Enrollment>();
        for (Enrollment e : enrollments) {
            if (e.getStatus() == EnrollmentStatus.COMPLETED && e.getGrade() != null) {
                graded.add(e);
            }
        }
        return weightedGpa(graded);
    }

    private double weightedGpa(ArrayList<Enrollment> graded) {
        int credits = 0;
        double points = 0.0;
        for (Enrollment e : graded) {
            int ch = e.getCourseSection().getCourse().getCreditHours();
            credits += ch;
            points += e.getGrade().getGradePoints() * ch;
        }
        if (credits == 0) {
            return 0.0;
        }
        return points / credits;
    }

    @Override
    public String toString() {
        return "AcademicRecord [enrollments = " + enrollments.size()
                + ", earnedCredits = " + getEarnedCredits() + "]";
    }
}
