package com.university.registration.service;

import com.university.registration.dto.SemesterSummary;
import com.university.registration.dto.Transcript;
import com.university.registration.model.AcademicRecord;
import com.university.registration.model.Enrollment;
import com.university.registration.model.Semester;
import com.university.registration.model.Student;
import com.university.registration.model.enums.EnrollmentStatus;

import java.util.Comparator;
import java.util.List;

/** Builds transcripts from a student's academic record. */
public class ReportService {

    public Transcript transcript(Student student) {
        AcademicRecord record = student.getAcademicRecord();

        List<Semester> semesters = record.getEnrollments().stream()
                .map(e -> e.getCourseSection().getSemester())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        List<SemesterSummary> summaries = semesters.stream()
                .map(semester -> new SemesterSummary(
                        semester,
                        record.getEnrollments(semester),
                        record.calculateGPA(semester),
                        attemptedCredits(record.getEnrollments(semester))))
                .toList();

        return new Transcript(student, summaries, record.calculateCGPA(), record.getEarnedCredits());
    }

    private int attemptedCredits(List<Enrollment> enrollments) {
        return enrollments.stream()
                .filter(e -> e.getStatus() != EnrollmentStatus.WITHDRAWN)
                .mapToInt(e -> e.getCourseSection().getCourse().getCreditHours())
                .sum();
    }
}
