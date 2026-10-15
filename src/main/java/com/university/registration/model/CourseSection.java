package com.university.registration.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a specific section of a course offered in a semester.
 * Tracks instructor, capacity, meetings, and enrolled students.
 */
public class CourseSection {

    private final String sectionId;
    private final Course course;
    private final Semester semester;
    private Instructor instructor;
    private final int capacity;
    private final List<MeetingTime> meetingTimes;
    private final List<Enrollment> enrollments;

    public CourseSection(String sectionId, Course course, Semester semester,
                         Instructor instructor, int capacity) {
        this.sectionId = Objects.requireNonNull(sectionId, "sectionId must not be null");
        this.course = Objects.requireNonNull(course, "course must not be null");
        this.semester = Objects.requireNonNull(semester, "semester must not be null");
        this.instructor = instructor;
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be at least 1");
        }
        this.capacity = capacity;
        this.meetingTimes = new ArrayList<>();
        this.enrollments = new ArrayList<>();
    }

    public String getSectionId() {
        return sectionId;
    }

    public Course getCourse() {
        return course;
    }

    public Semester getSemester() {
        return semester;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<MeetingTime> getMeetingTimes() {
        return Collections.unmodifiableList(meetingTimes);
    }

    public void addMeetingTime(MeetingTime meetingTime) {
        Objects.requireNonNull(meetingTime, "meetingTime must not be null");
        meetingTimes.add(meetingTime);
    }

    public List<Enrollment> getEnrollments() {
        return Collections.unmodifiableList(enrollments);
    }

    public void addEnrollment(Enrollment enrollment) {
        Objects.requireNonNull(enrollment, "enrollment must not be null");
        enrollments.add(enrollment);
    }

    /**
     * Checks if this section has available seats.
     *
     * @return true if enrolled count is below capacity
     */
    public boolean hasAvailableSeats() {
        return getAvailableSeatsCount() > 0;
    }

    /**
     * Returns the number of available seats in this section.
     *
     * @return the number of remaining seats
     */
    public int getAvailableSeatsCount() {
        long activeEnrollments = enrollments.stream()
                .filter(Enrollment::isActive)
                .count();
        return capacity - (int) activeEnrollments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseSection that)) return false;
        return Objects.equals(sectionId, that.sectionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sectionId);
    }

    @Override
    public String toString() {
        return "CourseSection{sectionId='" + sectionId + "', course=" + course.getCourseCode()
                + ", semester=" + semester + "}";
    }
}
