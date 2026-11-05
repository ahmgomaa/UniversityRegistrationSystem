package com.university.registration.dto;

import com.university.registration.model.Student;

import java.util.List;

/** Full transcript of a student: per-semester summaries plus cumulative figures. */
public record Transcript(Student student, List<SemesterSummary> semesters,
                         double cgpa, int earnedCredits) {

    public Transcript {
        semesters = List.copyOf(semesters == null ? List.of() : semesters);
    }
}
