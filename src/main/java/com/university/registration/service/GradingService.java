package com.university.registration.service;

import com.university.registration.model.CourseSection;
import com.university.registration.model.Enrollment;
import com.university.registration.model.Grade;
import com.university.registration.model.Instructor;
import com.university.registration.model.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service responsible for recording grades and retrieving class rosters.
 */
public class GradingService {

    private final NotificationService notificationService;

    public GradingService(NotificationService notificationService) {
        this.notificationService = Objects.requireNonNull(notificationService,
                "notificationService must not be null");
    }

    /**
     * Records a grade for a student's enrollment.
     * Only the instructor assigned to the section can record grades.
     *
     * @param instructor the instructor recording the grade
     * @param student    the student receiving the grade
     * @param enrollment the enrollment to grade
     * @param score      the numeric score (0-100)
     * @throws IllegalArgumentException if the instructor is not assigned to the section
     * @throws IllegalStateException    if the enrollment is not active
     */
    public void recordGrade(Instructor instructor, Student student,
                            Enrollment enrollment, int score) {
        Objects.requireNonNull(instructor, "instructor must not be null");
        Objects.requireNonNull(student, "student must not be null");
        Objects.requireNonNull(enrollment, "enrollment must not be null");

        CourseSection section = enrollment.getSection();
        if (!instructor.equals(section.getInstructor())) {
            throw new IllegalArgumentException(
                    "Instructor " + instructor.getUserId()
                            + " is not assigned to section " + section.getSectionId());
        }

        Grade grade = new Grade(score);
        enrollment.completeGrade(grade);

        notificationService.notify(student,
                "Grade recorded for " + section.getCourse().getCourseCode()
                        + ": " + grade.getLetterGrade()
                        + " (" + score + ")");
    }

    /**
     * Returns the list of active enrollments (roster) for a given instructor
     * across the specified sections.
     *
     * @param instructor the instructor whose sections to check
     * @param sections   the list of sections to search
     * @return list of active enrollments taught by the instructor
     */
    public List<Enrollment> getRoster(Instructor instructor, List<CourseSection> sections) {
        Objects.requireNonNull(instructor, "instructor must not be null");
        Objects.requireNonNull(sections, "sections must not be null");

        List<Enrollment> roster = new ArrayList<>();
        for (CourseSection section : sections) {
            if (instructor.equals(section.getInstructor())) {
                for (Enrollment enrollment : section.getEnrollments()) {
                    if (enrollment.isActive()) {
                        roster.add(enrollment);
                    }
                }
            }
        }
        return roster;
    }
}
