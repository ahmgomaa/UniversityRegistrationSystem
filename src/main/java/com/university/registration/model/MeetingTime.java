package com.university.registration.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a scheduled meeting time for a course section.
 */
public class MeetingTime {

    private final DayOfWeek day;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public MeetingTime(DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        this.day = Objects.requireNonNull(day, "day must not be null");
        this.startTime = Objects.requireNonNull(startTime, "startTime must not be null");
        this.endTime = Objects.requireNonNull(endTime, "endTime must not be null");
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
    }

    public DayOfWeek getDay() {
        return day;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Checks if this meeting time conflicts with another meeting time.
     * Two meetings conflict if they are on the same day and their time ranges overlap.
     *
     * @param other the other meeting time to check against
     * @return true if the meetings conflict
     */
    public boolean conflictsWith(MeetingTime other) {
        if (other == null || this.day != other.day) {
            return false;
        }
        return this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MeetingTime that)) return false;
        return day == that.day
                && Objects.equals(startTime, that.startTime)
                && Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, startTime, endTime);
    }

    @Override
    public String toString() {
        return "MeetingTime{" + day + " " + startTime + "-" + endTime + "}";
    }
}
