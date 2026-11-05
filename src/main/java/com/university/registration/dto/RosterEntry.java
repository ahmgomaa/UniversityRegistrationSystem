package com.university.registration.dto;

import com.university.registration.model.Enrollment;
import com.university.registration.model.Student;

/** One row of a section roster: the student and their enrollment. */
public record RosterEntry(Student student, Enrollment enrollment) {
}
