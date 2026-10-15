package com.university.registration.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a course offered by the university.
 * Courses have a code, name, credit value, description, and may have prerequisites.
 */
public class Course {

    private final String courseCode;
    private final String courseName;
    private final int credits;
    private final String description;
    private final Set<Course> prerequisites;

    public Course(String courseCode, String courseName, int credits, String description) {
        this.courseCode = Objects.requireNonNull(courseCode, "courseCode must not be null");
        this.courseName = Objects.requireNonNull(courseName, "courseName must not be null");
        if (credits < 1) {
            throw new IllegalArgumentException("credits must be at least 1");
        }
        this.credits = credits;
        this.description = description;
        this.prerequisites = new HashSet<>();
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getCredits() {
        return credits;
    }

    public String getDescription() {
        return description;
    }

    public Set<Course> getPrerequisites() {
        return Collections.unmodifiableSet(prerequisites);
    }

    /**
     * Adds a prerequisite course.
     *
     * @param prerequisite the course that must be completed before this one
     */
    public void addPrerequisite(Course prerequisite) {
        Objects.requireNonNull(prerequisite, "prerequisite must not be null");
        if (prerequisite.equals(this)) {
            throw new IllegalArgumentException("A course cannot be its own prerequisite");
        }
        prerequisites.add(prerequisite);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course course)) return false;
        return Objects.equals(courseCode, course.courseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseCode);
    }

    @Override
    public String toString() {
        return "Course{courseCode='" + courseCode + "', courseName='" + courseName
                + "', credits=" + credits + "}";
    }
}
