package com.university.registration.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A schedulable instance of a {@link Course} in a {@link Semester}, taught by
 * an {@link Instructor}, with weekly {@link MeetingTime}s and a capacity.
 */
public class CourseSection {

    private final String sectionId;
    private final Course course;
    private final Instructor instructor;
    private final Semester semester;
    private final List<MeetingTime> meetings;
    private final int capacity;
    private final String sectionNo;

    public CourseSection(String sectionId, Course course, Instructor instructor, Semester semester,
                         String sectionNo, int capacity, List<MeetingTime> meetings) {
        this.sectionId = Objects.requireNonNull(sectionId, "sectionId");
        this.course = Objects.requireNonNull(course, "course");
        this.instructor = Objects.requireNonNull(instructor, "instructor");
        this.semester = Objects.requireNonNull(semester, "semester");
        this.sectionNo = Objects.requireNonNull(sectionNo, "sectionNo");
        this.capacity = capacity;
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must not be negative");
        }
        this.meetings = meetings == null ? List.of() : List.copyOf(meetings);
    }

    public String getSectionId() {
        return sectionId;
    }

    public Course getCourse() {
        return course;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public Semester getSemester() {
        return semester;
    }

    public String getSectionNo() {
        return sectionNo;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<MeetingTime> getMeetings() {
        return Collections.unmodifiableList(meetings);
    }

    /**
     * Whether this section's timetable overlaps another section's timetable in
     * the same semester.
     */
    public boolean conflictsWith(CourseSection otherSection) {
        if (!semester.equals(otherSection.semester)) {
            return false;
        }
        return meetings.stream()
                .flatMap(m1 -> otherSection.meetings.stream().map(m1::conflictsWith))
                .anyMatch(Boolean::booleanValue);
    }

    public boolean hasSeat(int enrolledCount) {
        return enrolledCount < capacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseSection that)) return false;
        return sectionId.equals(that.sectionId);
    }

    @Override
    public int hashCode() {
        return sectionId.hashCode();
    }

    @Override
    public String toString() {
        return "%s-%s (%s)".formatted(course.getCourseCode(), sectionNo, String.join("; ", meetings.stream().map(Object::toString).toList()));
    }
}
