package com.university.registration.port;

import com.university.registration.model.CourseSection;
import com.university.registration.model.Semester;

import java.util.List;
import java.util.Optional;

/** Persistence port for course sections. */
public interface SectionRepository {

    List<CourseSection> findBySemester(Semester semester);

    Optional<CourseSection> findById(String sectionId);

    /** Number of currently ENROLLED students in the section (for capacity checks). */
    int countEnrolled(CourseSection section);
}
