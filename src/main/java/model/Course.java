package model;

import java.util.ArrayList;

public class Course {

    private final String courseCode;
    private final String courseName;
    private final int creditHours;
    private final ArrayList<Course> prerequisites;
    private final Department department;

    public Course(String courseCode, String courseName, int creditHours,
                  ArrayList<Course> prerequisites, Department department) {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code must not be empty");
        }
        if (courseName == null || courseName.trim().isEmpty()) {
            throw new IllegalArgumentException("Course name must not be empty");
        }
        if (creditHours <= 0) {
            throw new IllegalArgumentException("Credit hours must be positive");
        }
        if (department == null) {
            throw new IllegalArgumentException("Department must not be null");
        }
        this.courseCode = courseCode.trim().toUpperCase();
        this.courseName = courseName.trim();
        this.creditHours = creditHours;
        this.prerequisites = new ArrayList<Course>();
        if (prerequisites != null) {
            for (Course c : prerequisites) {
                this.prerequisites.add(c);
            }
        }
        this.department = department;
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

    public ArrayList<Course> getPrerequisites() {
        return prerequisites;
    }

    public Department getDepartment() {
        return department;
    }

    public boolean hasPrerequisites() {
        return !prerequisites.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Course) {
            Course other = (Course) obj;
            return this.courseCode.equals(other.courseCode);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return courseCode.hashCode();
    }

    @Override
    public String toString() {
        return courseCode + " - " + courseName
                + " | " + creditHours + " credit hours"
                + " | Dept: " + department.getCode();
    }
}
