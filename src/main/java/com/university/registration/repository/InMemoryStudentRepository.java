package com.university.registration.repository;

import com.university.registration.model.Student;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory thread-safe implementation of StudentRepository.
 */
public class InMemoryStudentRepository implements StudentRepository {

    private final Map<String, Student> studentsById = new ConcurrentHashMap<>();
    private final Map<String, Student> studentsByNumber = new ConcurrentHashMap<>();

    @Override
    public Optional<Student> findById(String userId) {
        if (userId == null) return Optional.empty();
        return Optional.ofNullable(studentsById.get(userId));
    }

    @Override
    public Optional<Student> findByStudentId(String studentNumber) {
        if (studentNumber == null) return Optional.empty();
        return Optional.ofNullable(studentsByNumber.get(studentNumber));
    }

    @Override
    public List<Student> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(studentsById.values()));
    }

    @Override
    public void save(Student student) {
        Objects.requireNonNull(student, "student must not be null");
        studentsById.put(student.getUserId(), student);
        studentsByNumber.put(student.getStudentNumber(), student);
    }
}
