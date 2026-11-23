package service;

import model.AcademicStaff;
import model.CourseSection;
import model.Enrollment;
import model.Grade;
import model.Instructor;
import model.Program;
import model.Student;
import model.enums.AcademicStatus;
import model.enums.StaffRole;
import port.NotificationService;
import port.StudentRepository;

import java.util.ArrayList;

public class GradingService {

    private final NotificationService notificationService;
    private final StudentRepository studentRepository;

    public GradingService(NotificationService notificationService,
                          StudentRepository studentRepository) {
        this.notificationService = notificationService;
        this.studentRepository = studentRepository;
    }

    public static class RosterEntry {

        private final Student student;
        private final Enrollment enrollment;

        public RosterEntry(Student student, Enrollment enrollment) {
            this.student = student;
            this.enrollment = enrollment;
        }

        public Student getStudent() {
            return student;
        }

        public Enrollment getEnrollment() {
            return enrollment;
        }
    }

    public void recordGrade(Instructor instructor, Student student,
                            Enrollment enrollment, int score) {
        if (!enrollment.getCourseSection().getInstructor().equals(instructor)) {
            throw new IllegalArgumentException("Only the instructor of the section can record grades");
        }
        if (!student.getAcademicRecord().contains(enrollment)) {
            throw new IllegalArgumentException("Enrollment does not belong to this student");
        }

        enrollment.complete(new Grade(score));
        studentRepository.save(student);
        notificationService.notify(student,
                "Grade recorded for "
                        + enrollment.getCourseSection().getCourse().getCourseCode());
        updateStanding(student);
    }

    public void updateStanding(Student student) {
        Program program = student.getProgram();
        double cgpa = student.getAcademicRecord().calculateCGPA();
        AcademicStatus current = student.getAcademicStatus();
        AcademicStatus next = current;

        if (current == AcademicStatus.ACTIVE && cgpa < program.getProbationCGPA()) {
            next = AcademicStatus.PROBATION;
        } else if (current == AcademicStatus.PROBATION
                && cgpa >= program.getProbationCGPA()) {
            next = AcademicStatus.ACTIVE;
        }

        if (next != current) {
            student.applyStanding(next);
            studentRepository.save(student);
            notificationService.notify(student,
                    "Academic standing updated to " + next);
        }
    }

    public ArrayList<RosterEntry> getRoster(Instructor instructor, CourseSection section) {
        if (!section.getInstructor().equals(instructor)) {
            throw new IllegalArgumentException("Only the instructor of the section can view its roster");
        }
        return buildRoster(section);
    }

    public ArrayList<RosterEntry> getRoster(AcademicStaff staff, CourseSection section) {
        if (!staff.hasRole(StaffRole.REGISTRAR)) {
            throw new IllegalArgumentException("Only a REGISTRAR can view a roster");
        }
        return buildRoster(section);
    }

    private ArrayList<RosterEntry> buildRoster(CourseSection section) {
        ArrayList<RosterEntry> roster = new ArrayList<RosterEntry>();
        for (Student s : studentRepository.findAll()) {
            for (Enrollment e : s.getAcademicRecord().getEnrollments()) {
                if (e.getCourseSection().equals(section)) {
                    roster.add(new RosterEntry(s, e));
                }
            }
        }
        return roster;
    }
}
