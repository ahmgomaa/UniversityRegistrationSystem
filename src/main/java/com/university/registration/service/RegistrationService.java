package com.university.registration.service;

import com.university.registration.exception.RegistrationException;
import com.university.registration.model.AcademicRecord;
import com.university.registration.model.Course;
import com.university.registration.model.CourseSection;
import com.university.registration.model.Enrollment;
import com.university.registration.model.Semester;
import com.university.registration.model.Student;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Service responsible for registering students into course sections
 * and processing withdrawals. Enforces business rules and sends notifications.
 */
public class RegistrationService {

    private final NotificationService notificationService;

    public RegistrationService(NotificationService notificationService) {
        this.notificationService = Objects.requireNonNull(notificationService,
                "notificationService must not be null");
    }

    /**
     * Registers a student into a course section.
     * <p>
     * Validates:
     * <ul>
     *   <li>Student's academic status allows registration</li>
     *   <li>Registration period is open for the section's semester</li>
     *   <li>Section has available seats</li>
     *   <li>Student has completed all prerequisites</li>
     *   <li>Student is not already enrolled in the same course this semester</li>
     * </ul>
     *
     * @param student the student to register
     * @param section the course section to register in
     * @param today   the current date (for registration period validation)
     * @return the newly created Enrollment
     * @throws RegistrationException if any validation rule is violated
     */
    public Enrollment register(Student student, CourseSection section, LocalDate today) {
        Objects.requireNonNull(student, "student must not be null");
        Objects.requireNonNull(section, "section must not be null");
        Objects.requireNonNull(today, "today must not be null");

        // Check academic standing
        if (!student.getAcademicStatus().canRegister()) {
            throw new RegistrationException(
                    "Student " + student.getStudentNumber()
                            + " cannot register with status: " + student.getAcademicStatus());
        }

        // Check registration period
        Semester semester = section.getSemester();
        if (!semester.isInRegistrationPeriod(today)) {
            throw new RegistrationException(
                    "Registration period is not open for " + semester);
        }

        // Check seat availability
        if (!section.hasAvailableSeats()) {
            throw new RegistrationException(
                    "No available seats in section " + section.getSectionId());
        }

        // Check prerequisites
        AcademicRecord record = student.getAcademicRecord();
        Course course = section.getCourse();
        for (Course prereq : course.getPrerequisites()) {
            if (!record.hasCompletedCourse(prereq)) {
                throw new RegistrationException(
                        "Missing prerequisite: " + prereq.getCourseCode());
            }
        }

        // Check duplicate enrollment in same course this semester
        if (record.hasEnrolledBefore(course, semester)) {
            throw new RegistrationException(
                    "Student is already enrolled in " + course.getCourseCode()
                            + " for " + semester);
        }

        // Create enrollment
        Enrollment enrollment = new Enrollment(section);
        section.addEnrollment(enrollment);
        record.addEnrollment(enrollment);

        notificationService.notify(student,
                "Successfully registered for " + course.getCourseCode()
                        + " - Section " + section.getSectionId());

        return enrollment;
    }

    /**
     * Withdraws a student from an enrollment.
     *
     * @param student    the student withdrawing
     * @param enrollment the enrollment to withdraw from
     * @param today      the current date
     * @throws RegistrationException if the enrollment is not active or withdrawal deadline has passed
     */
    public void withdraw(Student student, Enrollment enrollment, LocalDate today) {
        Objects.requireNonNull(student, "student must not be null");
        Objects.requireNonNull(enrollment, "enrollment must not be null");
        Objects.requireNonNull(today, "today must not be null");

        if (!enrollment.isActive()) {
            throw new RegistrationException("Enrollment is not active");
        }

        Semester semester = enrollment.getSection().getSemester();
        LocalDate withdrawDeadline = semester.getCourseWithdrawDeadline();
        if (withdrawDeadline != null && today.isAfter(withdrawDeadline)) {
            throw new RegistrationException(
                    "Withdrawal deadline has passed for " + semester);
        }

        enrollment.cancelRegistration(today);

        notificationService.notify(student,
                "Withdrawn from " + enrollment.getSection().getCourse().getCourseCode()
                        + " - Section " + enrollment.getSection().getSectionId());
    }
}
