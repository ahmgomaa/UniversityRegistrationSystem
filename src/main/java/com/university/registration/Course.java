package com.university.registration;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Course {
    private final String code;
    private final int capacity;
    private final Set<String> prerequisites;

    public Course(String code, int capacity, Set<String> prerequisites) {
        this.code = Objects.requireNonNull(code, "code must not be null");
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be at least 1");
        }
        this.capacity = capacity;
        this.prerequisites = new HashSet<>(Objects.requireNonNullElse(prerequisites, Collections.emptySet()));
    }

    public String getCode() {
        return code;
    }

    public int getCapacity() {
        return capacity;
    }

    public Set<String> getPrerequisites() {
        return Collections.unmodifiableSet(prerequisites);
    }
}
