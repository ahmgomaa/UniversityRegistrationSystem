package com.university.registration.service;

import com.university.registration.model.AcademicRecord;
import com.university.registration.model.Course;
import com.university.registration.model.CourseSection;
import com.university.registration.model.Enrollment;
import com.university.registration.model.Program;
import com.university.registration.model.Semester;
import com.university.registration.model.Student;
import com.university.registration.model.enums.AcademicStatus;
import com.university.registration.model.enums.EnrollmentStatus;
import com.university.registration.port.EnrollmentRepository;
import com.university.registration.port.NotificationService;
import com.university.registration.port.SectionRepository;
import com.university.registration.port.StudentRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Registration and withdrawal. Validates every business rule before touching
 * the student's record; all violations are collected and reported together in
 * one {@link RegistrationException}.
 */
public class RegistrationService {

    private final NotificationService notificationService;
    private final StudentRepository studentRepository;
    private final SectionRepository sectionRepository;
    private final EnrollmentRepository enrollmentRepository;

    public RegistrationService(NotificationService notificationService,
                               StudentRepository studentRepository,
                               SectionRepository sectionRepository,
                               EnrollmentRepository enrollmentRepository) {
        this.notificationService = notificationService;
        this.studentRepository = studentRepository;
        this.sectionRepository = sectionRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    /**
     * Registers a student in a section. Checks, in order: academic standing,
     * registration window, capacity, timetable conflicts, duplicate
     * enrollment, prerequisites and the semester credit limit (stricter while
     * on probation).
     *
     * @return the created enrollment
     * @throws RegistrationException with every violated rule
     */
    public Enrollment register(Student student, CourseSection section, LocalDate today) {
        List<String> errors = new ArrayList<>();
        AcademicRecord record = student.getAcademicRecord();
        Semester semester = section.getSemester();
        Program program = student.getProgram();
        AcademicStatus status = student.getAcademicStatus();

        if (!status.canRegister()) {
            errors.add("Cannot register: academic status is " + status);
        }
        if (!semester.getRegistrationPeriod().isOpen(today)) {
            errors.add("Registration period for " + semester + " is not open on " + today
                    + " (" + semester.getRegistrationPeriod() + ")");
        }
        int enrolledCount = sectionRepository.countEnrolled(section);
        if (!section.hasSeat(enrolledCount)) {
            errors.add("Section " + section + " is full (capacity " + section.getCapacity() + ")");
        }
        record.getEnrollments().stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ENROLLED)
                .map(Enrollment::getCourseSection)
                .filter(active -> active.conflictsWith(section))
                .forEach(conflicting -> errors.add(
                        "Timetable conflict with enrolled section " + conflicting));
        if (record.hasEnrolledIn(section.getCourse(), semester)) {
            errors.add("Already enrolled in " + section.getCourse().getCourseCode()
                    + " in " + semester);
        }
        for (Course prerequisite : section.getCourse().getPrerequisites()) {
            if (!record.hasPassed(prerequisite)) {
                errors.add("Missing prerequisite " + prerequisite.getCourseCode());
            }
        }
        int maxCredits = status == AcademicStatus.PROBATION
                ? program.getProbationMaxCredits()
                : program.getMaxCreditsPerSemester();
        int projectedCredits = record.getRegisteredCredits(semester)
                + section.getCourse().getCreditHours();
        if (projectedCredits > maxCredits) {
            errors.add("Credit limit exceeded: %d credits would exceed the %s limit of %d"
                    .formatted(projectedCredits, status, maxCredits));
        }

        if (!errors.isEmpty()) {
            throw new RegistrationException(errors);
        }

        Enrollment enrollment = new Enrollment(section, today);
        record.addEnrollment(enrollment);
        enrollmentRepository.save(student, enrollment);
        notificationService.notify(student,
                "Registered for " + section.getCourse().getCourseCode() + " section "
                        + section.getSectionNo() + " (" + semester + ")");
        return enrollment;
    }

    /**
     * Withdraws an active enrollment while the semester's withdrawal deadline
     * has not passed.
     */
    public void withdraw(Student student, Enrollment enrollment, LocalDate today) {
        List<String> errors = new ArrayList<>();

        if (!student.getAcademicRecord().contains(enrollment)) {
            errors.add("Enrollment does not belong to this student");
        }
        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            errors.add("Enrollment is not active (status: " + enrollment.getStatus() + ")");
        }
        Semester semester = enrollment.getCourseSection().getSemester();
        if (!semester.isWithdrawalAllowed(today)) {
            errors.add("Withdrawal from " + semester + " is not allowed on " + today
                    + " (deadline " + semester.getWithdrawalDeadline() + ")");
        }

        if (!errors.isEmpty()) {
            throw new RegistrationException(errors);
        }

        enrollment.withdraw(today);
        enrollmentRepository.save(student, enrollment);
        notificationService.notify(student,
                "Withdrawn from " + enrollment.getCourseSection().getCourse().getCourseCode()
                        + " (" + semester + ")");
    }
}
