package com.university.registration.dto;

import java.util.Collections;
import java.util.List;

/**
 * Represents a student's academic transcript containing all graded course entries.
 */
public class Transcript {

    private final String studentName;
    private final String studentNumber;
    private final List<TranscriptEntry> records;
    private final double cumulativeGPA;
    private final int totalCredits;

    public Transcript(String studentName, String studentNumber,
                      List<TranscriptEntry> records, double cumulativeGPA, int totalCredits) {
        this.studentName = studentName;
        this.studentNumber = studentNumber;
        this.records = records != null
                ? Collections.unmodifiableList(records)
                : Collections.emptyList();
        this.cumulativeGPA = cumulativeGPA;
        this.totalCredits = totalCredits;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public List<TranscriptEntry> getRecords() {
        return records;
    }

    public double getCumulativeGPA() {
        return cumulativeGPA;
    }

    public int getTotalCredits() {
        return totalCredits;
    }

    @Override
    public String toString() {
        return "Transcript{student='" + studentName + "', records=" + records.size()
                + ", GPA=" + String.format("%.2f", cumulativeGPA) + "}";
    }
}
