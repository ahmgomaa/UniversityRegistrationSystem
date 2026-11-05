package com.university.registration.service;

import com.university.registration.dto.RosterEntry;
import com.university.registration.model.AcademicRecord;
import com.university.registration.model.AcademicStaff;
import com.university.registration.model.CourseSection;
import com.university.registration.model.Enrollment;
import com.university.registration.model.Grade;
import com.university.registration.model.Instructor;
import com.university.registration.model.Program;
import com.university.registration.model.Student;
import com.university.registration.model.enums.AcademicStatus;
import com.university.registration.model.enums.StaffRole;
import com.university.registration.port.EnrollmentRepository;
import com.university.registration.port.NotificationService;
import com.university.registration.port.StudentRepository;

import java.util.List;

/**
 * Grade recording, academic standing transitions and section rosters.
 */
public class GradingService {

    private final NotificationService notificationService;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    public GradingService(NotificationService notificationService,
                          StudentRepository studentRepository,
                          EnrollmentRepository enrollmentRepository) {
        this.notificationService = notificationService;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    /**
     * Records the final grade for one enrollment. Only the instructor who
     * teaches the section may grade it. Recomputes the student's standing
     * afterwards.
     */
    public void recordGrade(Instructor instructor, Student student, Enrollment enrollment, double score) {
        if (!enrollment.getCourseSection().getInstructor().equals(instructor)) {
            throw new IllegalArgumentException(
                    "Only the instructor of " + enrollment.getCourseSection() + " can record grades");
        }
        if (!student.getAcademicRecord().contains(enrollment)) {
            throw new IllegalArgumentException("Enrollment does not belong to this student");
        }

        Grade grade = new Grade(score);
        enrollment.complete(grade);
        studentRepository.save(student);
        notificationService.notify(student,
                "Grade recorded for " + enrollment.getCourseSection().getCourse().getCourseCode()
                        + ": " + grade.getScore() + " (" + grade.getLetterGrade().display() + ")");

        updateStanding(student);
    }

    /**
     * Applies the program's standing policy: below probationCGPA an ACTIVE
     * student moves to PROBATION; a PROBATION student who recovers to at or
     * above probationCGPA returns to ACTIVE. Registrar-set statuses
     * (SUSPENDED, GRADUATED) are never changed here.
     */
    public void updateStanding(Student student) {
        Program program = student.getProgram();
        AcademicRecord record = student.getAcademicRecord();
        double cgpa = record.calculateCGPA();
        AcademicStatus current = student.getAcademicStatus();

        AcademicStatus next = switch (current) {
            case ACTIVE -> cgpa < program.getProbationCGPA() ? AcademicStatus.PROBATION : current;
            case PROBATION -> cgpa >= program.getProbationCGPA() ? AcademicStatus.ACTIVE : current;
            default -> current;  // SUSPENDED / GRADUATED: registrar decisions only
        };

        if (next != current) {
            student.applyStanding(next);
            studentRepository.save(student);
            notificationService.notify(student,
                    "Academic standing updated to " + next + " (CGPA " + String.format("%.2f", cgpa) + ")");
        }
    }

    /** Roster of a section, restricted to its instructor. */
    public List<RosterEntry> getRoster(Instructor instructor, CourseSection section) {
        if (!section.getInstructor().equals(instructor)) {
            throw new IllegalArgumentException("Only the instructor of the section can view its roster");
        }
        return enrollmentRepository.findRoster(section);
    }

    /**
     * Convenience overload used by the console: a staff member with the
     * REGISTRAR role may view any roster.
     */
    public List<RosterEntry> getRoster(AcademicStaff staff, CourseSection section) {
        if (!staff.hasRole(StaffRole.REGISTRAR)) {
            throw new IllegalArgumentException("Only the section instructor or a registrar can view a roster");
        }
        return enrollmentRepository.findRoster(section);
    }
}
