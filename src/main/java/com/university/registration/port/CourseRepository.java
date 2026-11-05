package com.university.registration.port;

import com.university.registration.model.Course;

import java.util.List;
import java.util.Optional;

/** Persistence port for the course catalog. */
public interface CourseRepository {

    List<Course> findAll();

    Optional<Course> findById(String courseCode);
}
