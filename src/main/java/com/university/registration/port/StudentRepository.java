package com.university.registration.port;

import com.university.registration.model.Student;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port for students (diagram: «interface» StudentRepository with
 * findAll/findById, extended with save for persistence).
 */
public interface StudentRepository {

    List<Student> findAll();

    Optional<Student> findById(String userId);

    /** Persists the student's profile, status, program and advisor link. */
    void save(Student student);
}
