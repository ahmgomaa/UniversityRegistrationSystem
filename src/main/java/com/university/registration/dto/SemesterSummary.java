package com.university.registration.dto;

import com.university.registration.model.Enrollment;
import com.university.registration.model.Semester;

import java.util.List;

/** One semester block of a transcript. */
public record SemesterSummary(Semester semester, List<Enrollment> enrollments,
                              double gpa, int registeredCredits) {

    public SemesterSummary {
        enrollments = List.copyOf(enrollments == null ? List.of() : enrollments);
    }
}
