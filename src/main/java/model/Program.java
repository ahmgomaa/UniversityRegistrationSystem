package model;

import java.util.ArrayList;

public class Program {

    private final int programId;
    private final String name;
    private final Department department;
    private final int totalCreditsRequired;
    private final double minimumCGPA;
    private final ArrayList<Course> requiredCourses;
    private final ArrayList<Course> electiveCourses;
    private final int electiveCredits;
    private final int maxCreditsPerSemester;
    private final int probationMaxCredits;
    private final double probationCGPA;
    private final int numberOfLevels;

    public Program(int programId, String name, Department department,
                   int totalCreditsRequired, double minimumCGPA,
                   ArrayList<Course> requiredCourses, ArrayList<Course> electiveCourses,
                   int electiveCredits, int maxCreditsPerSemester,
                   int probationMaxCredits, double probationCGPA, int numberOfLevels) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Program name must not be empty");
        }
        if (department == null) {
            throw new IllegalArgumentException("Department must not be null");
        }
        this.programId = programId;
        this.name = name.trim();
        this.department = department;
        this.totalCreditsRequired = totalCreditsRequired;
        this.minimumCGPA = minimumCGPA;
        this.requiredCourses = new ArrayList<Course>();
        if (requiredCourses != null) {
            for (Course c : requiredCourses) {
                this.requiredCourses.add(c);
            }
        }
        this.electiveCourses = new ArrayList<Course>();
        if (electiveCourses != null) {
            for (Course c : electiveCourses) {
                this.electiveCourses.add(c);
            }
        }
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

    public ArrayList<Course> getRequiredCourses() {
        return requiredCourses;
    }

    public ArrayList<Course> getElectiveCourses() {
        return electiveCourses;
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

    public int levelFor(int earnedCredits) {
        if (numberOfLevels <= 1) {
            return 1;
        }
        int creditsPerLevel = totalCreditsRequired / numberOfLevels;
        if (creditsPerLevel < 1) {
            creditsPerLevel = 1;
        }
        int level = 1 + earnedCredits / creditsPerLevel;
        if (level > numberOfLevels) {
            level = numberOfLevels;
        }
        return level;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Program) {
            Program other = (Program) obj;
            return this.name.equals(other.name)
                    && this.department.equals(other.department);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + department.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
