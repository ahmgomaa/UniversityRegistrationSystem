package com.university.registration.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents an academic program (e.g., Computer Science, Engineering).
 * Defines required and elective courses, credit requirements, and GPA thresholds.
 */
public class Program {

    private final String name;
    private final Department department;
    private final int totalCreditsRequired;
    private final List<Course> requiredCourses;
    private final List<Course> electiveCourses;
    private final int minCreditPerSemester;
    private final int maxCreditPerSemester;
    private final double probationGPA;

    public Program(String name, Department department, int totalCreditsRequired,
                   int minCreditPerSemester, int maxCreditPerSemester, double probationGPA) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.department = Objects.requireNonNull(department, "department must not be null");
        this.totalCreditsRequired = totalCreditsRequired;
        this.minCreditPerSemester = minCreditPerSemester;
        this.maxCreditPerSemester = maxCreditPerSemester;
        this.probationGPA = probationGPA;
        this.requiredCourses = new ArrayList<>();
        this.electiveCourses = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public Department getDepartment() {
        return department;
    }

    public int getTotalCreditsRequired() {
        return totalCreditsRequired;
    }

    public int getMinCreditPerSemester() {
        return minCreditPerSemester;
    }

    public int getMaxCreditPerSemester() {
        return maxCreditPerSemester;
    }

    public double getProbationGPA() {
        return probationGPA;
    }

    public List<Course> getRequiredCourses() {
        return Collections.unmodifiableList(requiredCourses);
    }

    public List<Course> getElectiveCourses() {
        return Collections.unmodifiableList(electiveCourses);
    }

    /**
     * Returns all courses (required + elective) in this program.
     */
    public List<Course> getAllCourses() {
        List<Course> all = new ArrayList<>(requiredCourses);
        all.addAll(electiveCourses);
        return Collections.unmodifiableList(all);
    }

    public void addRequiredCourse(Course course) {
        Objects.requireNonNull(course, "course must not be null");
        requiredCourses.add(course);
    }

    public void addElectiveCourse(Course course) {
        Objects.requireNonNull(course, "course must not be null");
        electiveCourses.add(course);
    }

    /**
     * Checks whether the given course is a required course in this program.
     */
    public boolean isRequiredCourse(Course course) {
        return requiredCourses.contains(course);
    }

    /**
     * Checks whether the given course is an elective course in this program.
     */
    public boolean isElectiveCourse(Course course) {
        return electiveCourses.contains(course);
    }

    @Override
    public String toString() {
        return "Program{name='" + name + "', department=" + department + "}";
    }
}
