package com.university.registration.service;

import com.university.registration.dto.Transcript;
import com.university.registration.dto.TranscriptEntry;
import com.university.registration.model.AcademicRecord;
import com.university.registration.model.Student;
import com.university.registration.model.enums.EnrollmentStatus;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service responsible for generating academic reports such as transcripts.
 */
public class ReportService {

        /**
         * Generates a transcript for the given student, containing all completed
         * courses.
         *
         * @param student the student to generate the transcript for
         * @return a Transcript containing all graded course entries
         */
        public Transcript transcriptReport(Student student) {
                Objects.requireNonNull(student, "student must not be null");

                AcademicRecord record = student.getAcademicRecord();

                List<TranscriptEntry> entries = record.getEnrollments().stream()
                                .filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED && e.getGrade() != null)
                                .map(e -> new TranscriptEntry(
                                                e.getSection().getCourse().getCourseName(),
                                                e.getSection().getCourse().getCourseCode(),
                                                e.getSection().getCourse().getCredits(),
                                                e.getGrade().getLetterGrade()))
                                .collect(Collectors.toList());

                return new Transcript(
                                student.getName(),
                                student.getStudentNumber(),
                                entries,
                                record.calculateCGPA(),
                                record.getEarnedCredits());
        }
}
