package com.university.registration.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a registration or add/withdraw period with start and end dates.
 */
public class RegistrationPeriod {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public RegistrationPeriod(LocalDate startDate, LocalDate endDate) {
        this.startDate = Objects.requireNonNull(startDate, "startDate must not be null");
        this.endDate = Objects.requireNonNull(endDate, "endDate must not be null");
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Checks whether the given date falls within this period (inclusive).
     *
     * @param date the date to check
     * @return true if the date is within the period
     */
    public boolean isOpen(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Checks if today's date falls within this period.
     *
     * @return true if today is within the period
     */
    public boolean isToday() {
        return isOpen(LocalDate.now());
    }

    @Override
    public String toString() {
        return "RegistrationPeriod{" + startDate + " to " + endDate + "}";
    }
}
