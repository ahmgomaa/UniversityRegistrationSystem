package data;

import model.AcademicStaff;
import model.Course;
import model.CourseSection;
import model.Department;
import model.Enrollment;
import model.Grade;
import model.Instructor;
import model.MeetingTime;
import model.Notification;
import model.Program;
import model.RegistrationPeriod;
import model.Semester;
import model.Student;
import model.enums.StaffRole;
import model.enums.Term;
import model.User;
import model.enums.AcademicStatus;
import model.enums.EnrollmentStatus;
import port.StudentRepository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class DataStore implements StudentRepository {

    private final Connection connection;

    private ArrayList<Department> departments = new ArrayList<Department>();
    private ArrayList<Course> courses = new ArrayList<Course>();
    private ArrayList<Program> programs = new ArrayList<Program>();
    private ArrayList<Semester> semesters = new ArrayList<Semester>();
    private ArrayList<CourseSection> sections = new ArrayList<CourseSection>();
    private ArrayList<Instructor> instructors = new ArrayList<Instructor>();
    private ArrayList<AcademicStaff> staff = new ArrayList<AcademicStaff>();
    private ArrayList<Student> students = new ArrayList<Student>();
    private ArrayList<Notification> notifications = new ArrayList<Notification>();

    public DataStore(Connection connection) {
        this.connection = connection;
    }

    public void load() throws SQLException {
        loadDepartments();
        loadCourses();
        loadPrerequisites();
        loadPrograms();
        loadProgramCourses();
        loadInstructorsAndStaff();
        loadSemesters();
        loadSections();
        loadMeetings();
        loadStudents();
        loadEnrollments();
        loadNotifications();
    }

    private void loadDepartments() throws SQLException {
        String sql = "SELECT dept_code, dept_name FROM departments";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                departments.add(new Department(rs.getString("dept_code"), rs.getString("dept_name")));
            }
        }
    }

    private void loadCourses() throws SQLException {
        String sql = "SELECT course_code, course_name, credit_hours, dept_code FROM courses";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Department department = findDepartment(rs.getString("dept_code"));
                courses.add(new Course(rs.getString("course_code"), rs.getString("course_name"),
                        rs.getInt("credit_hours"), null, department));
            }
        }
    }

    private void loadPrerequisites() throws SQLException {
        String sql = "SELECT course_code, prerequisite_code FROM course_prerequisites";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Course course = findCourse(rs.getString("course_code"));
                Course prerequisite = findCourse(rs.getString("prerequisite_code"));
                if (course != null && prerequisite != null) {
                    course.getPrerequisites().add(prerequisite);
                }
            }
        }
    }

    private void loadPrograms() throws SQLException {
        String sql = "SELECT program_id, name, dept_code, total_credits, min_cgpa, elective_credits, "
                + "max_credits_per_semester, probation_max_credits, probation_cgpa, number_of_levels FROM programs";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Department department = findDepartment(rs.getString("dept_code"));
                programs.add(new Program(rs.getInt("program_id"), rs.getString("name"), department,
                        rs.getInt("total_credits"), rs.getDouble("min_cgpa"),
                        null, null, rs.getInt("elective_credits"),
                        rs.getInt("max_credits_per_semester"), rs.getInt("probation_max_credits"),
                        rs.getDouble("probation_cgpa"), rs.getInt("number_of_levels")));
            }
        }
    }

    private void loadProgramCourses() throws SQLException {
        String sql = "SELECT program_id, course_code, course_kind FROM program_courses";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Program program = findProgram(rs.getInt("program_id"));
                Course course = findCourse(rs.getString("course_code"));
                if (program == null || course == null) {
                    continue;
                }
                if ("ELECTIVE".equals(rs.getString("course_kind"))) {
                    program.getElectiveCourses().add(course);
                } else {
                    program.getRequiredCourses().add(course);
                }
            }
        }
    }

    private void loadInstructorsAndStaff() throws SQLException {
        String sql = "SELECT u.user_id, u.name, u.phone, u.password_hash, u.user_type, "
                + "s.dept_code, s.roles FROM users u JOIN staff_members s ON u.user_id = s.user_id";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Department department = findDepartment(rs.getString("dept_code"));
                String type = rs.getString("user_type");
                if ("INSTRUCTOR".equals(type)) {
                    instructors.add(new Instructor(rs.getString("user_id"), rs.getString("name"),
                            rs.getString("phone"), User.HashedPassword.of(rs.getString("password_hash")), department));
                } else if ("STAFF".equals(type)) {
                    staff.add(new AcademicStaff(rs.getString("user_id"), rs.getString("name"),
                            rs.getString("phone"), User.HashedPassword.of(rs.getString("password_hash")), department,
                            parseRoles(rs.getString("roles"))));
                }
            }
        }
    }

    private ArrayList<StaffRole> parseRoles(String rolesText) {
        ArrayList<StaffRole> roles = new ArrayList<StaffRole>();
        if (rolesText == null || rolesText.isEmpty()) {
            return roles;
        }
        String[] parts = rolesText.split(",");
        for (String part : parts) {
            String value = part.trim();
            if (!value.isEmpty()) {
                roles.add(StaffRole.valueOf(value));
            }
        }
        return roles;
    }

    private void loadSemesters() throws SQLException {
        String sql = "SELECT semester_id, term, year, start_date, end_date, withdrawal_deadline, "
                + "registration_start, registration_end FROM semesters";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                LocalDate start = rs.getDate("start_date").toLocalDate();
                LocalDate end = rs.getDate("end_date").toLocalDate();
                LocalDate deadline = rs.getDate("withdrawal_deadline").toLocalDate();
                RegistrationPeriod period = new RegistrationPeriod(
                        rs.getDate("registration_start").toLocalDate(),
                        rs.getDate("registration_end").toLocalDate());
                semesters.add(new Semester(rs.getString("semester_id"), Term.valueOf(rs.getString("term")),
                        rs.getInt("year"), start, end, deadline, period));
            }
        }
    }

    private void loadSections() throws SQLException {
        String sql = "SELECT section_id, course_code, instructor_id, semester_id, section_no, capacity FROM course_sections";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Course course = findCourse(rs.getString("course_code"));
                Instructor instructor = findInstructorById(rs.getString("instructor_id"));
                Semester semester = findSemester(rs.getString("semester_id"));
                sections.add(new CourseSection(rs.getString("section_id"), course, instructor,
                        semester, rs.getString("section_no"), rs.getInt("capacity"), null));
            }
        }
    }

    private void loadMeetings() throws SQLException {
        String sql = "SELECT section_id, meeting_day, start_time, end_time, room FROM meeting_times";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                CourseSection section = findSectionById(rs.getString("section_id"));
                if (section == null) {
                    continue;
                }
                section.getMeetings().add(new MeetingTime(
                        java.time.DayOfWeek.valueOf(rs.getString("meeting_day")),
                        rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time").toLocalTime(),
                        rs.getString("room")));
            }
        }
    }

    private void loadStudents() throws SQLException {
        String sql = "SELECT u.user_id, u.name, u.phone, u.password_hash, s.student_number, "
                + "s.admission_year, s.academic_status, s.program_id, s.advisor_id "
                + "FROM students s JOIN users u ON s.user_id = u.user_id";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Program program = findProgram(rs.getInt("program_id"));
                AcademicStaff advisor = findStaffById(rs.getString("advisor_id"));
                students.add(new Student(rs.getString("user_id"), rs.getString("name"),
                        rs.getString("phone"), User.HashedPassword.of(rs.getString("password_hash")),
                        rs.getString("student_number"), rs.getInt("admission_year"),
                        AcademicStatus.valueOf(rs.getString("academic_status")), program, advisor));
            }
        }
    }

    private void loadEnrollments() throws SQLException {
        String sql = "SELECT user_id, section_id, status, score, enrolled_on, withdrawn_on FROM enrollments";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Student student = findById(rs.getString("user_id"));
                CourseSection section = findSectionById(rs.getString("section_id"));
                if (student == null || section == null) {
                    continue;
                }
                Enrollment enrollment = new Enrollment(section,
                        rs.getDate("enrolled_on").toLocalDate());
                EnrollmentStatus status = EnrollmentStatus.valueOf(rs.getString("status"));
                int score = rs.getInt("score");
                boolean hasScore = !rs.wasNull();
                Grade grade = null;
                if (hasScore) {
                    grade = new Grade(score);
                }
                Date withdrawnOn = rs.getDate("withdrawn_on");
                enrollment.restoreState(status, grade,
                        withdrawnOn == null ? null : withdrawnOn.toLocalDate());
                student.getAcademicRecord().addEnrollment(enrollment);
            }
        }
    }

    private void loadNotifications() throws SQLException {
        String sql = "SELECT recipient_id, message, sent_at FROM notifications";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                User recipient = findAnyUser(rs.getString("recipient_id"));
                if (recipient == null) {
                    continue;
                }
                Timestamp sentAt = rs.getTimestamp("sent_at");
                notifications.add(new Notification(recipient, rs.getString("message"),
                        sentAt.toLocalDateTime()));
            }
        }
    }

    public void save(Student student) {
        try {
            persistStudent(student);
            persistNotifications();
        } catch (SQLException e) {
            System.out.println("[WARN] Could not save data: " + e.getMessage());
        }
    }

    public void save() {
        try {
            for (Student student : students) {
                persistStudent(student);
            }
            persistNotifications();
        } catch (SQLException e) {
            System.out.println("[WARN] Could not save data: " + e.getMessage());
        }
    }

    private void persistStudent(Student student) throws SQLException {
        updateUserRow(student.getUserId(), student.getName(), student.getEmail(),
                student.getPhoneNumber(), student.getPasswordHash());
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE students SET academic_status = ?, advisor_id = ? WHERE user_id = ?")) {
            ps.setString(1, student.getAcademicStatus().name());
            if (student.getAdvisor() != null) {
                ps.setString(2, student.getAdvisor().getUserId());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setString(3, student.getUserId());
            ps.executeUpdate();
        }
        try (PreparedStatement delete = connection.prepareStatement(
                "DELETE FROM enrollments WHERE user_id = ?")) {
            delete.setString(1, student.getUserId());
            delete.executeUpdate();
        }
        for (Enrollment e : student.getAcademicRecord().getEnrollments()) {
            insertEnrollment(student.getUserId(), e);
        }
    }

    private void updateUserRow(String userId, String name, String email,
                               String phone, String passwordHash) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE users SET name = ?, email = ?, phone = ?, password_hash = ? WHERE user_id = ?")) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, passwordHash);
            ps.setString(5, userId);
            ps.executeUpdate();
        }
    }

    private void insertEnrollment(String userId, Enrollment enrollment) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO enrollments (user_id, section_id, status, score, enrolled_on, withdrawn_on) "
                        + "VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, userId);
            ps.setString(2, enrollment.getCourseSection().getSectionId());
            ps.setString(3, enrollment.getStatus().name());
            if (enrollment.getGrade() != null) {
                ps.setInt(4, enrollment.getGrade().getScore());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setDate(5, Date.valueOf(enrollment.getEnrolledOn()));
            if (enrollment.getWithdrawnOn() != null) {
                ps.setDate(6, Date.valueOf(enrollment.getWithdrawnOn()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.executeUpdate();
        }
    }

    private void persistNotifications() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM notifications");
        }
        for (Notification n : notifications) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO notifications (recipient_id, message, sent_at) VALUES (?, ?, ?)")) {
                ps.setString(1, n.getRecipient().getUserId());
                ps.setString(2, n.getMessage());
                ps.setTimestamp(3, Timestamp.valueOf(n.getSentAt()));
                ps.executeUpdate();
            }
        }
    }

    public ArrayList<Student> getStudents() {
        return students;
    }

    @Override
    public ArrayList<Student> findAll() {
        return students;
    }

    @Override
    public Student findById(String userId) {
        for (Student s : students) {
            if (s.getUserId().equals(userId)) {
                return s;
            }
        }
        return null;
    }

    public ArrayList<Department> getDepartments() {
        return departments;
    }

    public ArrayList<Course> getCourses() {
        return courses;
    }

    public ArrayList<Program> getPrograms() {
        return programs;
    }

    public ArrayList<Semester> getSemesters() {
        return semesters;
    }

    public ArrayList<CourseSection> getSections() {
        return sections;
    }

    public ArrayList<Instructor> getInstructors() {
        return instructors;
    }

    public ArrayList<AcademicStaff> getStaff() {
        return staff;
    }

    public ArrayList<Notification> getNotifications() {
        return notifications;
    }

    public Instructor findInstructorById(String userId) {
        for (Instructor i : instructors) {
            if (i.getUserId().equals(userId)) {
                return i;
            }
        }
        return null;
    }

    public AcademicStaff findStaffById(String userId) {
        for (AcademicStaff a : staff) {
            if (a.getUserId().equals(userId)) {
                return a;
            }
        }
        return null;
    }

    public CourseSection findSectionById(String sectionId) {
        for (CourseSection s : sections) {
            if (s.getSectionId().equals(sectionId)) {
                return s;
            }
        }
        return null;
    }

    public ArrayList<CourseSection> getSectionsInSemester(Semester semester) {
        ArrayList<CourseSection> result = new ArrayList<CourseSection>();
        for (CourseSection s : sections) {
            if (s.getSemester().equals(semester)) {
                result.add(s);
            }
        }
        return result;
    }

    private Department findDepartment(String code) {
        for (Department d : departments) {
            if (d.getCode().equals(code)) {
                return d;
            }
        }
        return null;
    }

    private Course findCourse(String code) {
        for (Course c : courses) {
            if (c.getCourseCode().equals(code)) {
                return c;
            }
        }
        return null;
    }

    private Program findProgram(int programId) {
        for (Program p : programs) {
            if (p.getProgramId() == programId) {
                return p;
            }
        }
        return null;
    }

    private Semester findSemester(String semesterId) {
        for (Semester s : semesters) {
            if (s.getSemesterId().equals(semesterId)) {
                return s;
            }
        }
        return null;
    }

    private User findAnyUser(String userId) {
        User user = findById(userId);
        if (user != null) {
            return user;
        }
        user = findInstructorById(userId);
        if (user != null) {
            return user;
        }
        return findStaffById(userId);
    }
}
