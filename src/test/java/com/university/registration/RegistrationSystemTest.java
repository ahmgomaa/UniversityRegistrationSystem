package com.university.registration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import com.university.registration.dto.GraduationReport;
import com.university.registration.dto.Transcript;
import com.university.registration.exception.RegistrationException;
import com.university.registration.model.*;
import com.university.registration.model.enums.*;
import com.university.registration.service.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RegistrationSystemTest {

    private AppNotificationService notificationService;
    private RegistrationService registrationService;
    private GradingService gradingService;
    private GraduationService graduationService;
    private ReportService reportService;

    private Department csDepartment;
    private Program csProgram;
    private Course cs101;
    private Course cs201;
    private Course cs301;
    private Semester fallSemester;
    private CourseSection cs101Section;
    private CourseSection cs201Section;
    private Instructor instructor;
    private Student student;
    private AcademicStaff registrar;
    private AcademicStaff advisor;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        // Services
        notificationService = new AppNotificationService();
        registrationService = new RegistrationService(notificationService);
        gradingService = new GradingService(notificationService);
        graduationService = new GraduationService();
        reportService = new ReportService();

        // Department & Program
        csDepartment = new Department("CS", "Computer Science");
        csProgram = new Program("Computer Science", csDepartment, 9, 3, 18, 2.0);

        // Courses
        cs101 = new Course("CS101", "Intro to Programming", 3, "Basics of programming");
        cs201 = new Course("CS201", "Data Structures", 3, "Lists, trees, graphs");
        cs201.addPrerequisite(cs101);
        cs301 = new Course("CS301", "Algorithms", 3, "Algorithm design");
        cs301.addPrerequisite(cs201);

        csProgram.addRequiredCourse(cs101);
        csProgram.addRequiredCourse(cs201);
        csProgram.addRequiredCourse(cs301);

        // Semester with open registration
        today = LocalDate.of(2025, 8, 15);
        RegistrationPeriod regPeriod = new RegistrationPeriod(
                LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 31));
        fallSemester = new Semester(Term.FALL, 2025,
                LocalDate.of(2025, 9, 1), LocalDate.of(2025, 12, 15),
                regPeriod,
                LocalDate.of(2025, 9, 15),
                LocalDate.of(2025, 10, 15));

        // Instructor
        instructor = new Instructor("inst1", "Dr. Smith", "smith@uni.edu",
                LocalDate.of(1975, 3, 10), "555-0001", csDepartment);

        // Course Sections
        cs101Section = new CourseSection("CS101-F25-01", cs101, fallSemester, instructor, 30);
        cs101Section.addMeetingTime(
                new MeetingTime(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 30)));
        cs101Section.addMeetingTime(
                new MeetingTime(DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(10, 30)));

        cs201Section = new CourseSection("CS201-F25-01", cs201, fallSemester, instructor, 2);

        // Academic Staff
        registrar = new AcademicStaff("staff1", "Jane Registrar", "jane@uni.edu",
                LocalDate.of(1980, 5, 20), "555-0002", csDepartment,
                Set.of(StaffRole.REGISTRAR));
        advisor = new AcademicStaff("staff2", "Bob Advisor", "bob@uni.edu",
                LocalDate.of(1982, 7, 12), "555-0003", csDepartment,
                Set.of(StaffRole.ADVISOR));

        // Student
        student = new Student("u1", "Alice Johnson", "alice@uni.edu",
                LocalDate.of(2003, 1, 15), "555-1000",
                "S2025001", 2025, csProgram);
    }

    @Nested
    class RegistrationTests {

        @Test
        void registerSucceedsWhenAllConditionsMet() {
            Enrollment enrollment = registrationService.register(student, cs101Section, today);

            assertNotNull(enrollment);
            assertTrue(enrollment.isActive());
            assertEquals(29, cs101Section.getAvailableSeatsCount());
        }

        @Test
        void registerFailsWhenPrerequisiteMissing() {
            RegistrationException ex = assertThrows(RegistrationException.class,
                    () -> registrationService.register(student, cs201Section, today));

            assertTrue(ex.getMessage().contains("prerequisite"));
        }

        @Test
        void registerFailsWhenCourseFull() {
            // Pre-complete CS101 so students can register for CS201
            Student s2 = new Student("u2", "Bob", "bob@s.edu",
                    LocalDate.of(2003, 2, 1), "555-1001", "S2025002", 2025, csProgram);
            Student s3 = new Student("u3", "Carol", "carol@s.edu",
                    LocalDate.of(2003, 3, 1), "555-1002", "S2025003", 2025, csProgram);

            // Give all three students completed CS101
            completeCS101(student);
            completeCS101(s2);
            completeCS101(s3);

            registrationService.register(student, cs201Section, today);
            registrationService.register(s2, cs201Section, today);

            RegistrationException ex = assertThrows(RegistrationException.class,
                    () -> registrationService.register(s3, cs201Section, today));

            assertTrue(ex.getMessage().contains("No available seats"));
        }

        @Test
        void registerFailsWhenRegistrationPeriodClosed() {
            LocalDate tooLate = LocalDate.of(2025, 9, 5);

            RegistrationException ex = assertThrows(RegistrationException.class,
                    () -> registrationService.register(student, cs101Section, tooLate));

            assertTrue(ex.getMessage().contains("not open"));
        }

        @Test
        void registerFailsWhenStudentSuspended() {
            student.updateAcademicStanding(registrar, AcademicStatus.SUSPENDED);

            RegistrationException ex = assertThrows(RegistrationException.class,
                    () -> registrationService.register(student, cs101Section, today));

            assertTrue(ex.getMessage().contains("cannot register"));
        }

        @Test
        void registerFailsWhenAlreadyEnrolledInSameCourse() {
            registrationService.register(student, cs101Section, today);

            CourseSection anotherSection = new CourseSection(
                    "CS101-F25-02", cs101, fallSemester, instructor, 30);

            RegistrationException ex = assertThrows(RegistrationException.class,
                    () -> registrationService.register(student, anotherSection, today));

            assertTrue(ex.getMessage().contains("already enrolled"));
        }
    }

    @Nested
    class WithdrawalTests {

        @Test
        void withdrawSucceedsBeforeDeadline() {
            Enrollment enrollment = registrationService.register(student, cs101Section, today);
            LocalDate withdrawDate = LocalDate.of(2025, 9, 10);

            registrationService.withdraw(student, enrollment, withdrawDate);

            assertEquals(EnrollmentStatus.WITHDRAWN, enrollment.getStatus());
            assertEquals(30, cs101Section.getAvailableSeatsCount());
        }

        @Test
        void withdrawFailsAfterDeadline() {
            Enrollment enrollment = registrationService.register(student, cs101Section, today);
            LocalDate afterDeadline = LocalDate.of(2025, 11, 1);

            assertThrows(RegistrationException.class,
                    () -> registrationService.withdraw(student, enrollment, afterDeadline));
        }
    }

    @Nested
    class GradingTests {

        @Test
        void recordGradeCompletesEnrollment() {
            Enrollment enrollment = registrationService.register(student, cs101Section, today);

            gradingService.recordGrade(instructor, student, enrollment, 85);

            assertEquals(EnrollmentStatus.COMPLETED, enrollment.getStatus());
            assertNotNull(enrollment.getGrade());
            assertEquals(LetterGrade.A, enrollment.getGrade().getLetterGrade());
        }

        @Test
        void recordGradeFailsForWrongInstructor() {
            Enrollment enrollment = registrationService.register(student, cs101Section, today);
            Instructor other = new Instructor("inst2", "Dr. Jones", "jones@uni.edu",
                    LocalDate.of(1980, 1, 1), "555-9999", csDepartment);

            assertThrows(IllegalArgumentException.class,
                    () -> gradingService.recordGrade(other, student, enrollment, 90));
        }
    }

    @Nested
    class AcademicStandingTests {

        @Test
        void advisorCanBeAssigned() {
            student.assignAdvisor(advisor);
            assertEquals(advisor, student.getAdvisor());
        }

        @Test
        void nonAdvisorCannotBeAssigned() {
            assertThrows(IllegalArgumentException.class,
                    () -> student.assignAdvisor(registrar));
        }

        @Test
        void registrarCanUpdateStanding() {
            student.updateAcademicStanding(registrar, AcademicStatus.PROBATION);
            assertEquals(AcademicStatus.PROBATION, student.getAcademicStatus());
        }

        @Test
        void nonRegistrarCannotUpdateStanding() {
            assertThrows(IllegalArgumentException.class,
                    () -> student.updateAcademicStanding(advisor, AcademicStatus.PROBATION));
        }
    }

    @Nested
    class GraduationTests {

        @Test
        void studentNotEligibleWithMissingCourses() {
            GraduationReport report = graduationService.checkGraduationEligibility(student);

            assertFalse(report.isEligible());
            assertFalse(report.getUnfulfilledReasons().isEmpty());
        }

        @Test
        void studentEligibleWhenAllRequirementsMet() {
            // Complete all required courses
            completeCS101(student);
            completeCS201(student);
            completeCS301(student);

            GraduationReport report = graduationService.checkGraduationEligibility(student);

            assertTrue(report.isEligible());
            assertTrue(report.getUnfulfilledReasons().isEmpty());
            assertEquals(3, report.getCompletedCourses().size());
        }
    }

    @Nested
    class TranscriptTests {

        @Test
        void transcriptReturnsCompletedCourses() {
            completeCS101(student);

            Transcript transcript = reportService.transcriptReport(student);

            assertEquals("Alice Johnson", transcript.getStudentName());
            assertEquals("S2025001", transcript.getStudentNumber());
            assertEquals(1, transcript.getRecords().size());
            assertEquals("CS101", transcript.getRecords().get(0).getCourseCode());
            assertEquals(3, transcript.getTotalCredits());
            assertTrue(transcript.getCumulativeGPA() > 0);
        }
    }

    @Nested
    class MeetingTimeTests {

        @Test
        void conflictDetectedForOverlappingTimes() {
            MeetingTime m1 = new MeetingTime(DayOfWeek.MONDAY,
                    LocalTime.of(9, 0), LocalTime.of(10, 30));
            MeetingTime m2 = new MeetingTime(DayOfWeek.MONDAY,
                    LocalTime.of(10, 0), LocalTime.of(11, 30));

            assertTrue(m1.conflictsWith(m2));
        }

        @Test
        void noConflictForDifferentDays() {
            MeetingTime m1 = new MeetingTime(DayOfWeek.MONDAY,
                    LocalTime.of(9, 0), LocalTime.of(10, 30));
            MeetingTime m2 = new MeetingTime(DayOfWeek.TUESDAY,
                    LocalTime.of(9, 0), LocalTime.of(10, 30));

            assertFalse(m1.conflictsWith(m2));
        }
    }

    @Nested
    class LetterGradeTests {

        @Test
        void fromScoreReturnsCorrectGrades() {
            assertEquals(LetterGrade.A_PLUS, LetterGrade.fromScore(95));
            assertEquals(LetterGrade.A, LetterGrade.fromScore(85));
            assertEquals(LetterGrade.B_PLUS, LetterGrade.fromScore(80));
            assertEquals(LetterGrade.D, LetterGrade.fromScore(50));
            assertEquals(LetterGrade.F, LetterGrade.fromScore(30));
        }

        @Test
        void invalidScoreThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> LetterGrade.fromScore(101));
            assertThrows(IllegalArgumentException.class, () -> LetterGrade.fromScore(-1));
        }
    }

    @Nested
    class NotificationTests {

        @Test
        void registrationSendsNotification() {
            registrationService.register(student, cs101Section, today);

            assertFalse(notificationService.getNotifications().isEmpty());
            assertTrue(notificationService.getNotifications().get(0).getMessage()
                    .contains("CS101"));
        }
    }

    // ─── Helper methods ──────────────────────────────────────────

    private void completeCS101(Student s) {
        CourseSection section = new CourseSection(
                "CS101-helper-" + s.getStudentNumber(), cs101, fallSemester, instructor, 100);
        Enrollment enrollment = new Enrollment(section);
        section.addEnrollment(enrollment);
        s.getAcademicRecord().addEnrollment(enrollment);
        enrollment.completeGrade(new Grade(85));
    }

    private void completeCS201(Student s) {
        CourseSection section = new CourseSection(
                "CS201-helper-" + s.getStudentNumber(), cs201, fallSemester, instructor, 100);
        Enrollment enrollment = new Enrollment(section);
        section.addEnrollment(enrollment);
        s.getAcademicRecord().addEnrollment(enrollment);
        enrollment.completeGrade(new Grade(80));
    }

    private void completeCS301(Student s) {
        CourseSection section = new CourseSection(
                "CS301-helper-" + s.getStudentNumber(), cs301, fallSemester, instructor, 100);
        Enrollment enrollment = new Enrollment(section);
        section.addEnrollment(enrollment);
        s.getAcademicRecord().addEnrollment(enrollment);
        enrollment.completeGrade(new Grade(90));
    }
}
