package service;

import model.AcademicRecord;
import model.Course;
import model.CourseSection;
import model.Enrollment;
import model.Program;
import model.Semester;
import model.Student;
import model.enums.AcademicStatus;
import model.enums.EnrollmentStatus;
import port.NotificationService;
import port.StudentRepository;

import java.time.LocalDate;
import java.util.ArrayList;

public class RegistrationService {

    private final NotificationService notificationService;
    private final StudentRepository studentRepository;

    public RegistrationService(NotificationService notificationService,
                               StudentRepository studentRepository) {
        this.notificationService = notificationService;
        this.studentRepository = studentRepository;
    }

    public Enrollment register(Student student, CourseSection section, LocalDate today)
            throws RegistrationException {
        ArrayList<String> errors = new ArrayList<String>();
        AcademicRecord record = student.getAcademicRecord();
        Semester semester = section.getSemester();
        Program program = student.getProgram();
        AcademicStatus status = student.getAcademicStatus();

        if (!status.canRegister()) {
            errors.add("Cannot register: academic status is " + status);
        }

        if (!semester.getRegistrationPeriod().isOpen(today)) {
            errors.add("Registration period for " + semester + " is not open on " + today);
        }

        int enrolledCount = countEnrolled(section);
        if (!section.hasSeat(enrolledCount)) {
            errors.add("Section is full (capacity " + section.getCapacity() + ")");
        }

        for (Enrollment e : record.getEnrollments()) {
            if (e.getStatus() == EnrollmentStatus.ENROLLED
                    && e.getCourseSection().conflictsWith(section)) {
                errors.add("Timetable conflict with enrolled section " + e.getCourseSection());
            }
        }

        if (record.hasEnrolledIn(section.getCourse(), semester)) {
            errors.add("Already enrolled in " + section.getCourse().getCourseCode()
                    + " in " + semester);
        }

        for (Course prerequisite : section.getCourse().getPrerequisites()) {
            if (!record.hasPassed(prerequisite)) {
                errors.add("Missing prerequisite " + prerequisite.getCourseCode());
            }
        }

        int maxCredits = program.getMaxCreditsPerSemester();
        if (status == AcademicStatus.PROBATION) {
            maxCredits = program.getProbationMaxCredits();
        }
        int projectedCredits = record.getRegisteredCredits(semester)
                + section.getCourse().getCreditHours();
        if (projectedCredits > maxCredits) {
            errors.add("Credit limit exceeded: " + projectedCredits
                    + " credits would exceed the " + status + " limit of " + maxCredits);
        }

        if (!errors.isEmpty()) {
            throw new RegistrationException(errors);
        }

        Enrollment enrollment = new Enrollment(section, today);
        record.addEnrollment(enrollment);
        studentRepository.save(student);
        notificationService.notify(student,
                "Registered for " + section.getCourse().getCourseCode()
                        + " section " + section.getSectionNo());
        return enrollment;
    }

    public void withdraw(Student student, Enrollment enrollment, LocalDate today)
            throws RegistrationException {
        ArrayList<String> errors = new ArrayList<String>();

        if (!student.getAcademicRecord().contains(enrollment)) {
            errors.add("Enrollment does not belong to this student");
        }
        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            errors.add("Enrollment is not active (status: " + enrollment.getStatus() + ")");
        }

        Semester semester = enrollment.getCourseSection().getSemester();
        if (!semester.isWithdrawalAllowed(today)) {
            errors.add("Withdrawal from " + semester + " is not allowed on " + today);
        }

        if (!errors.isEmpty()) {
            throw new RegistrationException(errors);
        }

        enrollment.withdraw(today);
        studentRepository.save(student);
        notificationService.notify(student,
                "Withdrawn from " + enrollment.getCourseSection().getCourse().getCourseCode());
    }

    private int countEnrolled(CourseSection section) {
        int count = 0;
        for (Student s : studentRepository.findAll()) {
            for (Enrollment e : s.getAcademicRecord().getEnrollments()) {
                if (e.getStatus() == EnrollmentStatus.ENROLLED
                        && e.getCourseSection().equals(section)) {
                    count++;
                }
            }
        }
        return count;
    }
}
