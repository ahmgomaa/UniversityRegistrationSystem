package com.university.registration.model;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * A course (catalog entry). Sections are the schedulable instances of a
 * course. A course can list other courses as prerequisites (recursive
 * association).
 */
public class Course {

    private final String courseCode;
    private final String courseName;
    private final int creditHours;
    private final Set<Course> prerequisites;
    private final Department department;

    public Course(String courseCode, String courseName, int creditHours,
                  Set<Course> prerequisites, Department department) {
        this.courseCode = Objects.requireNonNull(courseCode, "courseCode");
        this.courseName = Objects.requireNonNull(courseName, "courseName");
        this.creditHours = creditHours;
        this.prerequisites = prerequisites == null ? Set.of() : Set.copyOf(prerequisites);
        this.department = Objects.requireNonNull(department, "department");
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getCreditHours() {
        return creditHours;
    }

    public Set<Course> getPrerequisites() {
        return Collections.unmodifiableSet(prerequisites);
    }

    public Department getDepartment() {
        return department;
    }

    public boolean hasPrerequisites() {
        return !prerequisites.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course course)) return false;
        return courseCode.equals(course.courseCode);
    }

    @Override
    public int hashCode() {
        return courseCode.hashCode();
    }

    @Override
    public String toString() {
        return "%s %s (%d cr)".formatted(courseCode, courseName, creditHours);
    }
}
