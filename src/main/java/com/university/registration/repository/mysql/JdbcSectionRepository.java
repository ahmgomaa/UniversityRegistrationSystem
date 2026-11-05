package com.university.registration.repository.mysql;

import com.university.registration.db.DatabaseConnection;
import com.university.registration.model.Course;
import com.university.registration.model.CourseSection;
import com.university.registration.model.Instructor;
import com.university.registration.model.MeetingTime;
import com.university.registration.model.Semester;
import com.university.registration.port.CourseRepository;
import com.university.registration.port.SemesterRepository;
import com.university.registration.port.SectionRepository;
import com.university.registration.port.StaffRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcSectionRepository implements SectionRepository {

    private final DatabaseConnection db;
    private final CourseRepository courses;
    private final StaffRepository staff;
    private final SemesterRepository semesters;

    public JdbcSectionRepository(DatabaseConnection db, CourseRepository courses,
                                 StaffRepository staff, SemesterRepository semesters) {
        this.db = db;
        this.courses = courses;
        this.staff = staff;
        this.semesters = semesters;
    }

    @Override
    public List<CourseSection> findBySemester(Semester semester) {
        try (Connection c = db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM course_sections WHERE semester_id = ? ORDER BY course_code, section_no")) {
            ps.setString(1, semester.getSemesterId());
            try (ResultSet rs = ps.executeQuery()) {
                List<CourseSection> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(c, rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw Sql.wrap(e);
        }
    }

    @Override
    public Optional<CourseSection> findById(String sectionId) {
        try (Connection c = db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM course_sections WHERE section_id = ?")) {
            ps.setString(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(c, rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw Sql.wrap(e);
        }
    }

    @Override
    public int countEnrolled(CourseSection section) {
        try (Connection c = db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COUNT(*) FROM enrollments WHERE section_id = ? AND status = 'ENROLLED'")) {
            ps.setString(1, section.getSectionId());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw Sql.wrap(e);
        }
    }

    private CourseSection map(Connection c, ResultSet rs) throws SQLException {
        Course course = courses.findById(rs.getString("course_code"))
                .orElseThrow(() -> new IllegalStateException("Section has no course"));
        Instructor instructor = staff.findInstructorById(rs.getString("instructor_id"))
                .orElseThrow(() -> new IllegalStateException("Section has no instructor"));
        Semester semester = semesters.findById(rs.getString("semester_id"))
                .orElseThrow(() -> new IllegalStateException("Section has no semester"));

        List<MeetingTime> meetings = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT * FROM meeting_times WHERE section_id = ? ORDER BY day, start_time")) {
            ps.setString(1, rs.getString("section_id"));
            try (ResultSet mrs = ps.executeQuery()) {
                while (mrs.next()) {
                    meetings.add(new MeetingTime(
                            DayOfWeek.valueOf(mrs.getString("day")),
                            mrs.getObject("start_time", LocalTime.class),
                            mrs.getObject("end_time", LocalTime.class),
                            mrs.getString("room")));
                }
            }
        }
        return new CourseSection(
                rs.getString("section_id"),
                course,
                instructor,
                semester,
                rs.getString("section_no"),
                rs.getInt("capacity"),
                meetings);
    }
}
