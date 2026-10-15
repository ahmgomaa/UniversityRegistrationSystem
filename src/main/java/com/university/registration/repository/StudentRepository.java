package com.university.registration.repository;

import com.university.registration.model.Student;

import java.util.Optional;

/**
 * Repository interface for finding students.
 */
public interface StudentRepository {

    /**
     * Finds a student by their internal user ID.
     *
     * @param userId the user ID
     * @return an Optional containing the student if found
     */
    Optional<Student> findById(String userId);

    /**
     * Finds a student by their student number.
     *
     * @param studentNumber the student number
     * @return an Optional containing the student if found
     */
    Optional<Student> findByStudentId(String studentNumber);
}
