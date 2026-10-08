package com.university.registration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RegistrationService {
    private final Map<String, Student> studentsById = new HashMap<>();
    private final Map<String, Course> coursesByCode = new HashMap<>();
    private final Map<String, Set<String>> enrollmentsByCourse = new HashMap<>();

    public void addStudent(Student student) {
        studentsById.put(student.getId(), student);
    }

    public void addCourse(Course course) {
        coursesByCode.put(course.getCode(), course);
        enrollmentsByCourse.putIfAbsent(course.getCode(), new HashSet<>());
    }

    public RegistrationResult register(String studentId, String courseCode) {
        Student student = studentsById.get(studentId);
        if (student == null) {
            return RegistrationResult.STUDENT_NOT_FOUND;
        }

        Course course = coursesByCode.get(courseCode);
        if (course == null) {
            return RegistrationResult.COURSE_NOT_FOUND;
        }

        Set<String> enrolledStudents = enrollmentsByCourse.computeIfAbsent(courseCode, ignored -> new HashSet<>());
        if (enrolledStudents.contains(studentId)) {
            return RegistrationResult.ALREADY_REGISTERED;
        }

        if (enrolledStudents.size() >= course.getCapacity()) {
            return RegistrationResult.COURSE_FULL;
        }

        if (!student.getCompletedCourses().containsAll(course.getPrerequisites())) {
            return RegistrationResult.MISSING_PREREQUISITES;
        }

        enrolledStudents.add(studentId);
        return RegistrationResult.SUCCESS;
    }

    public boolean drop(String studentId, String courseCode) {
        Set<String> enrolledStudents = enrollmentsByCourse.get(courseCode);
        return enrolledStudents != null && enrolledStudents.remove(studentId);
    }

    public boolean isRegistered(String studentId, String courseCode) {
        Set<String> enrolledStudents = enrollmentsByCourse.get(courseCode);
        return enrolledStudents != null && enrolledStudents.contains(studentId);
    }

    public int getEnrolledCount(String courseCode) {
        Set<String> enrolledStudents = enrollmentsByCourse.get(courseCode);
        return enrolledStudents == null ? 0 : enrolledStudents.size();
    }
}
