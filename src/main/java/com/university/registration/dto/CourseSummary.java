package com.university.registration.dto;

import com.university.registration.model.Semester;

/**
 * Summary of a student's performance in a single semester.
 */
public class CourseSummary {

    private final Semester semester;
    private final int registeredCredits;
    private final double gpa;

    public CourseSummary(Semester semester, int registeredCredits, double gpa) {
        this.semester = semester;
        this.registeredCredits = registeredCredits;
        this.gpa = gpa;
    }

    public Semester getSemester() {
        return semester;
    }

    public int getRegisteredCredits() {
        return registeredCredits;
    }

    public double getGpa() {
        return gpa;
    }

    @Override
    public String toString() {
        return "CourseSummary{semester=" + semester
                + ", credits=" + registeredCredits
                + ", GPA=" + String.format("%.2f", gpa) + "}";
    }
}
