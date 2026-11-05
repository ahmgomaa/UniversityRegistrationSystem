package com.university.registration.port;

import com.university.registration.dto.RosterEntry;
import com.university.registration.model.CourseSection;
import com.university.registration.model.Enrollment;
import com.university.registration.model.Student;

import java.util.List;

/** Persistence port for enrollments. */
public interface EnrollmentRepository {

    /** Inserts or updates one enrollment row of the given student. */
    void save(Student student, Enrollment enrollment);

    /** Active (ENROLLED) students of a section with their enrollments, sorted by name. */
    List<RosterEntry> findRoster(CourseSection section);
}
