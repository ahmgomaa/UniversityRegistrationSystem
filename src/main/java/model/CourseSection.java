package model;

import java.util.ArrayList;

public class CourseSection {

    private final String sectionId;
    private final Course course;
    private final Instructor instructor;
    private final Semester semester;
    private final String sectionNo;
    private final int capacity;
    private final ArrayList<MeetingTime> meetings;

    public CourseSection(String sectionId, Course course, Instructor instructor,
                         Semester semester, String sectionNo, int capacity,
                         ArrayList<MeetingTime> meetings) {
        if (sectionId == null || sectionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Section id must not be empty");
        }
        if (course == null || instructor == null || semester == null) {
            throw new IllegalArgumentException("Course, instructor and semester are required");
        }
        if (sectionNo == null || sectionNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Section number must not be empty");
        }
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must not be negative");
        }
        this.sectionId = sectionId.trim();
        this.course = course;
        this.instructor = instructor;
        this.semester = semester;
        this.sectionNo = sectionNo.trim();
        this.capacity = capacity;
        this.meetings = new ArrayList<MeetingTime>();
        if (meetings != null) {
            for (MeetingTime m : meetings) {
                this.meetings.add(m);
            }
        }
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

    public ArrayList<MeetingTime> getMeetings() {
        return meetings;
    }

    public boolean conflictsWith(CourseSection otherSection) {
        if (!semester.equals(otherSection.semester)) {
            return false;
        }
        for (MeetingTime mine : meetings) {
            for (MeetingTime theirs : otherSection.meetings) {
                if (mine.conflictsWith(theirs)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasSeat(int enrolledCount) {
        return enrolledCount < capacity;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof CourseSection) {
            CourseSection other = (CourseSection) obj;
            return this.sectionId.equals(other.sectionId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return sectionId.hashCode();
    }

    @Override
    public String toString() {
        String text = course.getCourseCode() + "-" + sectionNo;
        for (MeetingTime m : meetings) {
            text += " | " + m;
        }
        return text;
    }
}
