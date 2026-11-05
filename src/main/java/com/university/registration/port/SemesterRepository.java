package com.university.registration.port;

import com.university.registration.model.Semester;

import java.util.List;
import java.util.Optional;

/** Persistence port for semesters. */
public interface SemesterRepository {

    List<Semester> findAll();

    Optional<Semester> findById(String semesterId);
}
