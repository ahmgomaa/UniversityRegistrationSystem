package com.university.registration.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

/** Weekly meeting slot of a course section (day, time range, room). */
public class MeetingTime {

    private final DayOfWeek day;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String room;

    public MeetingTime(DayOfWeek day, LocalTime startTime, LocalTime endTime, String room) {
        this.day = Objects.requireNonNull(day, "day");
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.endTime = Objects.requireNonNull(endTime, "endTime");
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Meeting start time must be before end time");
        }
        this.room = Objects.requireNonNull(room, "room");
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

    public String getRoom() {
        return room;
    }

    /** Two meetings conflict when they are on the same day and their time ranges overlap. */
    public boolean conflictsWith(MeetingTime other) {
        return day == other.day
                && startTime.isBefore(other.endTime)
                && other.startTime.isBefore(endTime);
    }

    @Override
    public String toString() {
        return "%s %s-%s @ %s".formatted(day, startTime, endTime, room);
    }
}
