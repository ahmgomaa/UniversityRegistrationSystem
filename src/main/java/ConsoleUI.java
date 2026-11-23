import data.DataStore;
import model.AcademicStaff;
import model.Course;
import model.CourseSection;
import model.Enrollment;
import model.Instructor;
import model.Notification;
import model.Semester;
import model.Student;
import model.User;
import model.enums.AcademicStatus;
import model.enums.EnrollmentStatus;
import service.GraduationService;
import service.GraduationService.GraduationReport;
import service.GradingService;
import service.GradingService.RosterEntry;
import service.InAppNotificationService;
import service.RegistrationException;
import service.RegistrationService;
import service.ReportService;
import service.ReportService.SemesterSummary;
import service.ReportService.Transcript;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class ConsoleUI {

    private final DataStore store;
    private final RegistrationService registration;
    private final GradingService grading;
    private final GraduationService graduation;
    private final ReportService reports;
    private final InAppNotificationService notifications;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleUI(DataStore store,
                     RegistrationService registration,
                     GradingService grading,
                     GraduationService graduation,
                     ReportService reports,
                     InAppNotificationService notifications) {
        this.store = store;
        this.registration = registration;
        this.grading = grading;
        this.graduation = graduation;
        this.reports = reports;
        this.notifications = notifications;
    }

    public void run() {
        System.out.println("==============================================================");
        System.out.println("          UNIVERSITY STUDENT REGISTRATION SYSTEM              ");
        System.out.println("==============================================================");

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("--- MAIN MENU ---");
            System.out.println("1. Sign In (Student / Instructor / Academic Staff)");
            System.out.println("2. Browse Course Sections by Semester");
            System.out.println("0. Exit");
            System.out.print("Select an option: ");

            String choice = readLine();
            if (choice == null) {
                break;
            }
            if (choice.equals("1")) {
                signInFlow();
            } else if (choice.equals("2")) {
                browseSectionsFlow();
            } else if (choice.equals("0")) {
                System.out.println("Goodbye!");
                running = false;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private void signInFlow() {
        System.out.print("\nUser ID (e.g. CS-1001, IN-101, AS-11): ");
        String userId = readLine();
        if (userId == null || userId.isEmpty()) {
            return;
        }
        System.out.print("Password (4 digits, demo: 1234): ");
        String password = readLine();
        if (password == null) {
            return;
        }

        User user = findUserById(userId.trim());
        if (user == null || !user.verifyPassword(password.trim())) {
            System.out.println("[ACCESS DENIED] Invalid user ID or password.");
            return;
        }

        System.out.println();
        System.out.println("[WELCOME] " + user.getName() + " (" + user.getUserId() + ")");

        if (user instanceof Student) {
            Student student = (Student) user;
            studentPortal(student);
        } else if (user instanceof Instructor) {
            Instructor instructor = (Instructor) user;
            instructorPortal(instructor);
        } else if (user instanceof AcademicStaff) {
            AcademicStaff staff = (AcademicStaff) user;
            staffPortal(staff);
        }
    }

    private User findUserById(String id) {
        User user = store.findById(id);
        if (user != null) {
            return user;
        }
        user = store.findInstructorById(id);
        if (user != null) {
            return user;
        }
        return store.findStaffById(id);
    }

    private void studentPortal(Student student) {
        boolean signedIn = true;
        while (signedIn) {
            System.out.println();
            System.out.println("--- STUDENT PORTAL: " + student.getName()
                    + " [" + student.getStudentNumber() + "] ---");
            System.out.println("Status: " + student.getAcademicStatus()
                    + " | Level: " + student.getLevel()
                    + " | Program: " + student.getProgram().getName()
                    + " | Advisor: " + advisorName(student));
            System.out.println("1. Register in a Course Section");
            System.out.println("2. Withdraw from an Active Enrollment");
            System.out.println("3. View Transcript");
            System.out.println("4. Check Graduation Eligibility");
            System.out.println("5. View Notifications ("
                    + notifications.getInbox(student).size() + ")");
            System.out.println("6. Change Password");
            System.out.println("0. Sign Out");
            System.out.print("Select an option: ");

            String choice = readLine();
            if (choice == null || choice.equals("0")) {
                signedIn = false;
            } else if (choice.equals("1")) {
                registerInSection(student);
            } else if (choice.equals("2")) {
                withdrawFromSection(student);
            } else if (choice.equals("3")) {
                printTranscript(student);
            } else if (choice.equals("4")) {
                printGraduationEligibility(student);
            } else if (choice.equals("5")) {
                printNotifications(student);
            } else if (choice.equals("6")) {
                changePassword(student);
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private String advisorName(Student student) {
        if (student.getAdvisor() == null) {
            return "None";
        }
        return student.getAdvisor().getName();
    }

    private void registerInSection(Student student) {
        browseSectionsFlow();
        System.out.print("Enter Section ID (e.g. SEC-CS201-F26-01): ");
        String sectionId = readLine();
        if (sectionId == null || sectionId.isEmpty()) {
            return;
        }
        CourseSection section = store.findSectionById(sectionId.trim());
        if (section == null) {
            System.out.println("[ERROR] Section not found: " + sectionId);
            return;
        }
        try {
            Enrollment e = registration.register(student, section, LocalDate.now());
            store.save();
            System.out.println("[SUCCESS] Enrolled in " + e.getCourseSection());
        } catch (RegistrationException ex) {
            System.out.println("[REGISTRATION FAILED]");
            for (String err : ex.getErrors()) {
                System.out.println("  - " + err);
            }
        }
    }

    private void withdrawFromSection(Student student) {
        ArrayList<Enrollment> active = new ArrayList<Enrollment>();
        for (Enrollment e : student.getAcademicRecord().getEnrollments()) {
            if (e.getStatus() == EnrollmentStatus.ENROLLED) {
                active.add(e);
            }
        }
        if (active.isEmpty()) {
            System.out.println("No active enrollments.");
            return;
        }
        for (Enrollment e : active) {
            System.out.println(" - " + e.getCourseSection().getSectionId()
                    + ": " + e.getCourseSection());
        }
        System.out.print("Enter Section ID to withdraw from: ");
        String sectionId = readLine();
        if (sectionId == null || sectionId.isEmpty()) {
            return;
        }
        Enrollment target = null;
        for (Enrollment e : active) {
            if (e.getCourseSection().getSectionId().equalsIgnoreCase(sectionId.trim())) {
                target = e;
            }
        }
        if (target == null) {
            System.out.println("[ERROR] Active enrollment not found.");
            return;
        }
        try {
            registration.withdraw(student, target, LocalDate.now());
            store.save();
            System.out.println("[SUCCESS] Withdrawn from " + sectionId);
        } catch (RegistrationException ex) {
            System.out.println("[WITHDRAWAL FAILED] " + ex.getMessage());
        }
    }

    private void changePassword(User user) {
        System.out.print("Current 4-digit password: ");
        String oldPassword = readLine();
        System.out.print("New 4-digit password: ");
        String newPassword = readLine();
        if (oldPassword == null || newPassword == null) {
            return;
        }
        try {
            user.changePassword(oldPassword.trim(), newPassword.trim());
            if (user instanceof Student) {
                store.save();
            }
            System.out.println("[SUCCESS] Password updated.");
        } catch (IllegalArgumentException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }
    }

    private void printNotifications(Student student) {
        ArrayList<Notification> list = notifications.getInbox(student);
        if (list.isEmpty()) {
            System.out.println("No notifications.");
            return;
        }
        for (Notification n : list) {
            System.out.println(" " + n);
        }
    }

    private void instructorPortal(Instructor instructor) {
        boolean signedIn = true;
        while (signedIn) {
            System.out.println();
            System.out.println("--- INSTRUCTOR PORTAL: " + instructor.getName() + " ---");
            System.out.println("1. View Assigned Sections & Roster");
            System.out.println("2. Record Student Grade");
            System.out.println("0. Sign Out");
            System.out.print("Select an option: ");
            String choice = readLine();
            if (choice == null || choice.equals("0")) {
                signedIn = false;
            } else if (choice.equals("1")) {
                viewInstructorSections(instructor);
            } else if (choice.equals("2")) {
                recordGradeFlow(instructor);
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private void viewInstructorSections(Instructor instructor) {
        for (Semester semester : sortedSemesters()) {
            for (CourseSection section : store.getSectionsInSemester(semester)) {
                if (section.getInstructor().equals(instructor)) {
                    ArrayList<RosterEntry> roster = grading.getRoster(instructor, section);
                    System.out.println();
                    System.out.println(section.getSectionId() + " | " + section
                            + " [" + semester + "] ("
                            + roster.size() + "/" + section.getCapacity() + ")");
                    for (RosterEntry entry : roster) {
                        System.out.println("  * " + entry.getStudent().getUserId()
                                + " - " + entry.getStudent().getName()
                                + " [" + entry.getEnrollment().getStatus() + "]");
                    }
                }
            }
        }
    }

    private void recordGradeFlow(Instructor instructor) {
        viewInstructorSections(instructor);
        System.out.print("\nSection ID: ");
        String sectionId = readLine();
        System.out.print("Student ID (e.g. CS-1001): ");
        String studentId = readLine();
        System.out.print("Score (0 - 100): ");
        String scoreText = readLine();
        if (sectionId == null || studentId == null || scoreText == null) {
            return;
        }
        try {
            int score = Integer.parseInt(scoreText.trim());
            Student student = store.findById(studentId.trim());
            if (student == null) {
                System.out.println("[ERROR] Student not found.");
                return;
            }
            Enrollment enrollment = null;
            for (Enrollment e : student.getAcademicRecord().getEnrollments()) {
                if (e.getCourseSection().getSectionId().equalsIgnoreCase(sectionId.trim())) {
                    enrollment = e;
                }
            }
            if (enrollment == null) {
                System.out.println("[ERROR] Enrollment not found in this section.");
                return;
            }
            grading.recordGrade(instructor, student, enrollment, score);
            store.save();
            System.out.println("[SUCCESS] Grade recorded: " + enrollment.getGrade());
        } catch (NumberFormatException ex) {
            System.out.println("[ERROR] Score must be an integer between 0 and 100.");
        } catch (IllegalArgumentException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        } catch (IllegalStateException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }
    }

    private void staffPortal(AcademicStaff member) {
        boolean signedIn = true;
        while (signedIn) {
            System.out.println();
            System.out.println("--- ACADEMIC STAFF PORTAL: " + member.getName()
                    + " " + member.getRoles() + " ---");
            System.out.println("1. View All Students");
            System.out.println("2. Update Student Academic Status [REGISTRAR]");
            System.out.println("3. Assign Advisor to Student [ADVISOR]");
            System.out.println("4. View Section Roster [REGISTRAR]");
            System.out.println("0. Sign Out");
            System.out.print("Select an option: ");
            String choice = readLine();
            if (choice == null || choice.equals("0")) {
                signedIn = false;
            } else if (choice.equals("1")) {
                viewAllStudents();
            } else if (choice.equals("2")) {
                updateStudentStatus(member);
            } else if (choice.equals("3")) {
                assignAdvisor(member);
            } else if (choice.equals("4")) {
                viewRosterAsRegistrar(member);
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private void viewAllStudents() {
        for (Student s : store.findAll()) {
            System.out.println(" - " + s.getUserId() + " | " + s.getName()
                    + " | " + s.getStudentNumber()
                    + " | Status: " + s.getAcademicStatus()
                    + " | CGPA: " + round2(s.getAcademicRecord().calculateCGPA())
                    + " | Credits: " + s.getAcademicRecord().getEarnedCredits());
        }
    }

    private void updateStudentStatus(AcademicStaff member) {
        System.out.print("Student ID (e.g. CS-1001): ");
        String studentId = readLine();
        System.out.print("New Status (ACTIVE, PROBATION, SUSPENDED, GRADUATED): ");
        String statusText = readLine();
        if (studentId == null || statusText == null) {
            return;
        }
        try {
            Student student = store.findById(studentId.trim());
            if (student == null) {
                System.out.println("[ERROR] Student not found.");
                return;
            }
            AcademicStatus status = AcademicStatus.valueOf(statusText.trim().toUpperCase());
            student.updateAcademicStatus(member, status);
            store.save();
            System.out.println("[SUCCESS] Updated status for " + student.getName());
        } catch (IllegalArgumentException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }
    }

    private void assignAdvisor(AcademicStaff member) {
        if (!member.hasRole(model.enums.StaffRole.ADVISOR)) {
            System.out.println("[ERROR] ADVISOR role required.");
            return;
        }
        System.out.print("Student ID (e.g. CS-1001): ");
        String studentId = readLine();
        if (studentId == null) {
            return;
        }
        try {
            Student student = store.findById(studentId.trim());
            if (student == null) {
                System.out.println("[ERROR] Student not found.");
                return;
            }
            student.assignAdvisor(member);
            store.save();
            System.out.println("[SUCCESS] Assigned " + member.getName() + " to " + student.getName());
        } catch (IllegalArgumentException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }
    }

    private void viewRosterAsRegistrar(AcademicStaff member) {
        if (!member.hasRole(model.enums.StaffRole.REGISTRAR)) {
            System.out.println("[ERROR] REGISTRAR role required.");
            return;
        }
        System.out.print("Section ID: ");
        String sectionId = readLine();
        if (sectionId == null) {
            return;
        }
        try {
            CourseSection section = store.findSectionById(sectionId.trim());
            if (section == null) {
                System.out.println("[ERROR] Section not found.");
                return;
            }
            for (RosterEntry r : grading.getRoster(member, section)) {
                System.out.println("  * " + r.getStudent().getUserId()
                        + " - " + r.getStudent().getName()
                        + " [" + r.getEnrollment().getStatus() + "]");
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("[ERROR] " + ex.getMessage());
        }
    }

    private void browseSectionsFlow() {
        for (Semester semester : sortedSemesters()) {
            System.out.println();
            System.out.println("=== Semester: " + semester
                    + " (" + semester.getRegistrationPeriod() + ") ===");
            for (CourseSection section : store.getSectionsInSemester(semester)) {
                int enrolled = countEnrolled(section);
                System.out.println("  " + section.getSectionId() + " | " + section
                        + " | Instructor: " + section.getInstructor().getName()
                        + " | Seats: " + enrolled + "/" + section.getCapacity());
            }
        }
    }

    private void printTranscript(Student student) {
        Transcript transcript = reports.transcript(student);
        System.out.println();
        System.out.println("================ OFFICIAL TRANSCRIPT ================");
        System.out.println("Student : " + student.getName() + " (" + student.getStudentNumber() + ")");
        System.out.println("Program : " + student.getProgram().getName());
        System.out.println("Status  : " + student.getAcademicStatus()
                + " | Level: " + student.getLevel()
                + " | CGPA: " + round2(transcript.getCgpa())
                + " | Earned Credits: " + transcript.getEarnedCredits());
        for (SemesterSummary summary : transcript.getSemesters()) {
            System.out.println("  [" + summary.getSemester() + "] Semester GPA: "
                    + round2(summary.getGpa())
                    + " | Attempted Credits: " + summary.getRegisteredCredits());
            for (Enrollment e : summary.getEnrollments()) {
                Course c = e.getCourseSection().getCourse();
                String gradeText = "";
                if (e.getGrade() != null) {
                    gradeText = " | Grade: " + e.getGrade();
                }
                System.out.println("    * " + c.getCourseCode() + " - " + c.getCourseName()
                        + " (" + c.getCreditHours() + " cr) | " + e.getStatus() + gradeText);
            }
        }
        System.out.println("=====================================================");
    }

    private void printGraduationEligibility(Student student) {
        GraduationReport report = graduation.checkGraduationEligibility(student);
        String decision = "NO";
        if (report.isEligible()) {
            decision = "YES";
        }
        System.out.println("Graduation Eligible: " + decision);
        for (String reason : report.getUnmetReasons()) {
            System.out.println("  - " + reason);
        }
    }

    private ArrayList<Semester> sortedSemesters() {
        ArrayList<Semester> list = new ArrayList<Semester>();
        for (Semester s : store.getSemesters()) {
            list.add(s);
        }
        Collections.sort(list);
        return list;
    }

    private int countEnrolled(CourseSection section) {
        int count = 0;
        for (Student s : store.findAll()) {
            for (Enrollment e : s.getAcademicRecord().getEnrollments()) {
                if (e.getStatus() == EnrollmentStatus.ENROLLED
                        && e.getCourseSection().equals(section)) {
                    count++;
                }
            }
        }
        return count;
    }

    private String round2(double value) {
        return "" + (Math.round(value * 100.0) / 100.0);
    }

    private String readLine() {
        if (scanner.hasNextLine()) {
            return scanner.nextLine().trim();
        }
        return null;
    }
}
