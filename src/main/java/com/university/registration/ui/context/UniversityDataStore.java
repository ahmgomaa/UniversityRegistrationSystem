package com.university.registration.ui.context;

import com.university.registration.model.*;
import com.university.registration.model.enums.*;
import com.university.registration.repository.InMemoryStudentRepository;
import com.university.registration.repository.StudentRepository;
import com.university.registration.service.*;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central state store and service orchestrator for the University UI.
 * Pre-populates sample data and allows views to react to domain state changes.
 */
public class UniversityDataStore {

    private static UniversityDataStore instance;

    private final StudentRepository studentRepository;
    private final AppNotificationService notificationService;
    private final RegistrationService registrationService;
    private final GradingService gradingService;
    private final GraduationService graduationService;
    private final ReportService reportService;

    private final List<Department> departments = new ArrayList<>();
    private final List<Program> programs = new ArrayList<>();
    private final List<Course> courses = new ArrayList<>();
    private final List<CourseSection> courseSections = new ArrayList<>();
    private final List<Instructor> instructors = new ArrayList<>();
    private final List<AcademicStaff> academicStaffList = new ArrayList<>();
    private final List<Semester> semesters = new ArrayList<>();

    private final ObjectProperty<LocalDate> simulatedDate =
            new SimpleObjectProperty<>(LocalDate.of(2025, 8, 15));

    private final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();

    private UniversityDataStore() {
        this.studentRepository = new InMemoryStudentRepository();
        this.notificationService = new AppNotificationService();
        this.registrationService = new RegistrationService(notificationService);
        this.gradingService = new GradingService(notificationService);
        this.graduationService = new GraduationService();
        this.reportService = new ReportService();

        initSampleData();
    }

    public static synchronized UniversityDataStore getInstance() {
        if (instance == null) {
            instance = new UniversityDataStore();
        }
        return instance;
    }

    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    public void notifyStateChanged() {
        for (Runnable listener : changeListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initSampleData() {
        // Departments
        Department csDept = new Department("CS", "Computer Science & Engineering");
        Department mathDept = new Department("MATH", "Mathematics & Statistics");
        Department busDept = new Department("BUS", "Business Administration");
        departments.addAll(List.of(csDept, mathDept, busDept));

        // Program
        Program csProgram = new Program("B.Sc. Computer Science", csDept, 18, 3, 18, 2.0);
        programs.add(csProgram);

        // Courses
        Course cs101 = new Course("CS101", "Introduction to Computer Science", 3,
                "Fundamentals of programming, control structures, and algorithms.");
        Course cs102 = new Course("CS102", "Object-Oriented Programming", 3,
                "Encapsulation, inheritance, polymorphism, and Java fundamentals.");
        cs102.addPrerequisite(cs101);

        Course cs201 = new Course("CS201", "Data Structures & Algorithms", 4,
                "Lists, stacks, queues, trees, graphs, sorting, and complexity.");
        cs201.addPrerequisite(cs102);

        Course cs202 = new Course("CS202", "Database Systems", 3,
                "Relational models, SQL, normalization, transactions, and indexing.");
        cs202.addPrerequisite(cs102);

        Course cs301 = new Course("CS301", "Software Engineering Principles", 3,
                "Software lifecycles, agile, design patterns, testing, and CI/CD.");
        cs301.addPrerequisite(cs201);

        Course cs302 = new Course("CS302", "Operating Systems", 4,
                "Processes, threads, synchronization, memory management, file systems.");
        cs302.addPrerequisite(cs201);

        Course cs401 = new Course("CS401", "Senior Capstone Project", 3,
                "Comprehensive team project applying full-stack and architectural design.");
        cs401.addPrerequisite(cs301);
        cs401.addPrerequisite(cs202);

        courses.addAll(List.of(cs101, cs102, cs201, cs202, cs301, cs302, cs401));

        csProgram.addRequiredCourse(cs101);
        csProgram.addRequiredCourse(cs102);
        csProgram.addRequiredCourse(cs201);
        csProgram.addRequiredCourse(cs202);
        csProgram.addRequiredCourse(cs301);
        csProgram.addElectiveCourse(cs302);
        csProgram.addElectiveCourse(cs401);

        // Semesters
        RegistrationPeriod fallReg = new RegistrationPeriod(
                LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 31));
        Semester fall2025 = new Semester(Term.FALL, 2025,
                LocalDate.of(2025, 9, 1), LocalDate.of(2025, 12, 20),
                fallReg,
                LocalDate.of(2025, 9, 15),
                LocalDate.of(2025, 10, 20));

        RegistrationPeriod springReg = new RegistrationPeriod(
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 31));
        Semester spring2026 = new Semester(Term.SPRING, 2026,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 5, 30),
                springReg,
                LocalDate.of(2026, 2, 15),
                LocalDate.of(2026, 3, 20));

        semesters.addAll(List.of(fall2025, spring2026));

        // Instructors
        Instructor instTuring = new Instructor("inst_turing", "Dr. Alan Turing",
                "alan.turing@university.edu", LocalDate.of(1972, 6, 23), "555-0101", csDept);
        Instructor instHopper = new Instructor("inst_hopper", "Dr. Grace Hopper",
                "grace.hopper@university.edu", LocalDate.of(1975, 12, 9), "555-0102", csDept);
        Instructor instLovelace = new Instructor("inst_lovelace", "Dr. Ada Lovelace",
                "ada.lovelace@university.edu", LocalDate.of(1982, 12, 10), "555-0103", csDept);
        instructors.addAll(List.of(instTuring, instHopper, instLovelace));

        // Academic Staff
        AcademicStaff staffAdvisor = new AcademicStaff("staff_brody", "Prof. Marcus Brody",
                "marcus.brody@university.edu", LocalDate.of(1968, 4, 15), "555-0201",
                csDept, Set.of(StaffRole.ADVISOR));
        AcademicStaff staffRegistrar = new AcademicStaff("staff_connor", "Sarah Connor",
                "registrar@university.edu", LocalDate.of(1979, 11, 28), "555-0202",
                csDept, Set.of(StaffRole.REGISTRAR));
        AcademicStaff staffDual = new AcademicStaff("staff_henderson", "Dr. Peter Henderson",
                "peter.h@university.edu", LocalDate.of(1974, 8, 19), "555-0203",
                csDept, Set.of(StaffRole.ADVISOR, StaffRole.REGISTRAR));
        academicStaffList.addAll(List.of(staffAdvisor, staffRegistrar, staffDual));

        // Course Sections
        CourseSection sec101 = new CourseSection("CS101-F25-01", cs101, fall2025, instTuring, 25);
        sec101.addMeetingTime(new MeetingTime(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 30)));
        sec101.addMeetingTime(new MeetingTime(DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(10, 30)));

        CourseSection sec102 = new CourseSection("CS102-F25-01", cs102, fall2025, instHopper, 20);
        sec102.addMeetingTime(new MeetingTime(DayOfWeek.TUESDAY, LocalTime.of(11, 0), LocalTime.of(12, 30)));
        sec102.addMeetingTime(new MeetingTime(DayOfWeek.THURSDAY, LocalTime.of(11, 0), LocalTime.of(12, 30)));

        CourseSection sec201 = new CourseSection("CS201-F25-01", cs201, fall2025, instLovelace, 2); // small capacity for testing full
        sec201.addMeetingTime(new MeetingTime(DayOfWeek.MONDAY, LocalTime.of(13, 0), LocalTime.of(14, 30)));
        sec201.addMeetingTime(new MeetingTime(DayOfWeek.WEDNESDAY, LocalTime.of(13, 0), LocalTime.of(14, 30)));

        CourseSection sec202 = new CourseSection("CS202-F25-01", cs202, fall2025, instHopper, 20);
        sec202.addMeetingTime(new MeetingTime(DayOfWeek.TUESDAY, LocalTime.of(14, 0), LocalTime.of(15, 30)));

        CourseSection sec301 = new CourseSection("CS301-F25-01", cs301, fall2025, instLovelace, 15);
        sec301.addMeetingTime(new MeetingTime(DayOfWeek.MONDAY, LocalTime.of(15, 0), LocalTime.of(16, 30)));

        CourseSection sec101Spring = new CourseSection("CS101-S26-01", cs101, spring2026, instTuring, 30);
        sec101Spring.addMeetingTime(new MeetingTime(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 30)));

        courseSections.addAll(List.of(sec101, sec102, sec201, sec202, sec301, sec101Spring));

        // Students
        Student alice = new Student("u_alice", "Alice Walker", "alice.w@university.edu",
                LocalDate.of(2004, 3, 14), "555-1001", "STU202501", 2024, csProgram);
        alice.assignAdvisor(staffAdvisor);

        // Pre-complete CS101 and CS102 for Alice
        completeCourseDirect(alice, sec101, cs101, 88); // A
        completeCourseDirect(alice, sec102, cs102, 82); // B+

        Student bob = new Student("u_bob", "Bob Martin", "bob.m@university.edu",
                LocalDate.of(2003, 7, 21), "555-1002", "STU202502", 2023, csProgram);
        bob.assignAdvisor(staffAdvisor);
        completeCourseDirect(bob, sec101, cs101, 78); // B
        completeCourseDirect(bob, sec102, cs102, 86); // A
        completeCourseDirect(bob, sec201, cs201, 91); // A+
        completeCourseDirect(bob, sec202, cs202, 81); // B+

        Student charlie = new Student("u_charlie", "Charlie Brown", "charlie.b@university.edu",
                LocalDate.of(2005, 9, 5), "555-1003", "STU202503", 2025, csProgram);
        charlie.assignAdvisor(staffAdvisor);

        Student diana = new Student("u_diana", "Diana Prince", "diana.p@university.edu",
                LocalDate.of(2002, 5, 12), "555-1004", "STU202504", 2022, csProgram);
        diana.assignAdvisor(staffAdvisor);
        // Complete full degree requirements for Diana
        completeCourseDirect(diana, sec101, cs101, 95);
        completeCourseDirect(diana, sec102, cs102, 92);
        completeCourseDirect(diana, sec201, cs201, 89);
        completeCourseDirect(diana, sec202, cs202, 94);
        completeCourseDirect(diana, sec301, cs301, 90);
        // Elective
        CourseSection sec302 = new CourseSection("CS302-F25-01", cs302, fall2025, instTuring, 20);
        courseSections.add(sec302);
        completeCourseDirect(diana, sec302, cs302, 88);

        Student ethan = new Student("u_ethan", "Ethan Hunt", "ethan.h@university.edu",
                LocalDate.of(2004, 11, 2), "555-1005", "STU202505", 2024, csProgram);
        ethan.assignAdvisor(staffAdvisor);
        ethan.updateAcademicStanding(staffRegistrar, AcademicStatus.PROBATION);

        studentRepository.save(alice);
        studentRepository.save(bob);
        studentRepository.save(charlie);
        studentRepository.save(diana);
        studentRepository.save(ethan);
    }

    private void completeCourseDirect(Student student, CourseSection section, Course course, int score) {
        Enrollment enrollment = new Enrollment(section);
        section.addEnrollment(enrollment);
        student.getAcademicRecord().addEnrollment(enrollment);
        enrollment.completeGrade(new Grade(score));
    }

    // Getters and helper methods
    public StudentRepository getStudentRepository() {
        return studentRepository;
    }

    public AppNotificationService getNotificationService() {
        return notificationService;
    }

    public RegistrationService getRegistrationService() {
        return registrationService;
    }

    public GradingService getGradingService() {
        return gradingService;
    }

    public GraduationService getGraduationService() {
        return graduationService;
    }

    public ReportService getReportService() {
        return reportService;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public List<Program> getPrograms() {
        return programs;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public List<CourseSection> getCourseSections() {
        return courseSections;
    }

    public List<Instructor> getInstructors() {
        return instructors;
    }

    public List<AcademicStaff> getAcademicStaffList() {
        return academicStaffList;
    }

    public List<Semester> getSemesters() {
        return semesters;
    }

    public ObjectProperty<LocalDate> simulatedDateProperty() {
        return simulatedDate;
    }

    public LocalDate getSimulatedDate() {
        return simulatedDate.get();
    }

    public void setSimulatedDate(LocalDate date) {
        simulatedDate.set(date);
        notifyStateChanged();
    }

    public void addCourse(Course course) {
        courses.add(course);
        notifyStateChanged();
    }

    public void addCourseSection(CourseSection section) {
        courseSections.add(section);
        notifyStateChanged();
    }

    public void addStudent(Student student) {
        studentRepository.save(student);
        notifyStateChanged();
    }
}
