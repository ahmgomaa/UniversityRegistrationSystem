package com.university.registration.dto;

import com.university.registration.model.enums.LetterGrade;

/**
 * A single entry in a student's transcript, representing one completed course.
 */
public class TranscriptEntry {

    private final String courseName;
    private final String courseCode;
    private final int credits;
    private final LetterGrade grade;

    public TranscriptEntry(String courseName, String courseCode, int credits, LetterGrade grade) {
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.credits = credits;
        this.grade = grade;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public int getCredits() {
        return credits;
    }

    public LetterGrade getGrade() {
        return grade;
    }

    @Override
    public String toString() {
        return courseCode + " - " + courseName + " (" + credits + " cr): " + grade;
    }
}
