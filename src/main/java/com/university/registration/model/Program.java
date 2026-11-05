package com.university.registration.model;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Degree program a student follows. Encodes the graduation requirements and
 * the registration limits, including the stricter limits that apply while the
 * student is on probation.
 */
public class Program {

    private final int programId;
    private final String name;
    private final Department department;
    private final int totalCreditsRequired;
    private final double minimumCGPA;
    private final Set<Course> requiredCourses;
    private final Set<Course> electiveCourses;
    private final int electiveCredits;
    private final int maxCreditsPerSemester;
    private final int probationMaxCredits;
    private final double probationCGPA;
    private final int numberOfLevels;

    public Program(int programId, String name, Department department,
                   int totalCreditsRequired, double minimumCGPA,
                   Set<Course> requiredCourses, Set<Course> electiveCourses,
                   int electiveCredits, int maxCreditsPerSemester,
                   int probationMaxCredits, double probationCGPA, int numberOfLevels) {
        this.programId = programId;
        this.name = Objects.requireNonNull(name, "name");
        this.department = Objects.requireNonNull(department, "department");
        this.totalCreditsRequired = totalCreditsRequired;
        this.minimumCGPA = minimumCGPA;
        this.requiredCourses = requiredCourses == null ? Set.of() : Set.copyOf(requiredCourses);
        this.electiveCourses = electiveCourses == null ? Set.of() : Set.copyOf(electiveCourses);
        this.electiveCredits = electiveCredits;
        this.maxCreditsPerSemester = maxCreditsPerSemester;
        this.probationMaxCredits = probationMaxCredits;
        this.probationCGPA = probationCGPA;
        this.numberOfLevels = numberOfLevels;
    }

    public int getProgramId() {
        return programId;
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

    public double getMinimumCGPA() {
        return minimumCGPA;
    }

    public Set<Course> getRequiredCourses() {
        return Collections.unmodifiableSet(requiredCourses);
    }

    public Set<Course> getElectiveCourses() {
        return Collections.unmodifiableSet(electiveCourses);
    }

    public int getElectiveCredits() {
        return electiveCredits;
    }

    public int getMaxCreditsPerSemester() {
        return maxCreditsPerSemester;
    }

    public int getProbationMaxCredits() {
        return probationMaxCredits;
    }

    public double getProbationCGPA() {
        return probationCGPA;
    }

    public int getNumberOfLevels() {
        return numberOfLevels;
    }

    public boolean isRequired(Course course) {
        return requiredCourses.contains(course);
    }

    public boolean isElective(Course course) {
        return electiveCourses.contains(course);
    }

    /**
     * Maps earned credits to a level (year) within the program. Credits are
     * spread evenly across {@code numberOfLevels}; a student with no credits
     * is level 1, one who completed everything is the last level.
     */
    public int levelFor(int earnedCredits) {
        if (numberOfLevels <= 1) {
            return 1;
        }
        int creditsPerLevel = Math.max(1, totalCreditsRequired / numberOfLevels);
        int level = 1 + Math.floorDiv(Math.max(0, earnedCredits), creditsPerLevel);
        return Math.min(level, numberOfLevels);
    }

    @Override
    public String toString() {
        return name;
    }
}
