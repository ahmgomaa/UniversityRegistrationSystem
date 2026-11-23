package model;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class MeetingTime {

    private final DayOfWeek day;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String room;

    public MeetingTime(DayOfWeek day, LocalTime startTime, LocalTime endTime, String room) {
        if (day == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("Day and times must not be null");
        }
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Meeting start time must be before end time");
        }
        if (room == null || room.trim().isEmpty()) {
            throw new IllegalArgumentException("Room must not be empty");
        }
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room.trim();
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

    public boolean conflictsWith(MeetingTime other) {
        return day == other.day
                && startTime.isBefore(other.endTime)
                && other.startTime.isBefore(endTime);
    }

    @Override
    public String toString() {
        return day + " " + startTime + "-" + endTime + " Room: " + room;
    }
}
