package com.university.registration;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Student {
    private final String id;
    private final Set<String> completedCourses;

    public Student(String id, Set<String> completedCourses) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.completedCourses = new HashSet<>(Objects.requireNonNullElse(completedCourses, Collections.emptySet()));
    }

    public String getId() {
        return id;
    }

    public Set<String> getCompletedCourses() {
        return Collections.unmodifiableSet(completedCourses);
    }
}
