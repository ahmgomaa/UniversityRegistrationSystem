package com.university.registration.model;

import java.time.LocalDate;
import java.util.Objects;

/** Window during which registration (and changes) are allowed. */
public class RegistrationPeriod {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public RegistrationPeriod(LocalDate startDate, LocalDate endDate) {
        this.startDate = Objects.requireNonNull(startDate, "startDate");
        this.endDate = Objects.requireNonNull(endDate, "endDate");
        if (!startDate.isBefore(endDate) && !startDate.equals(endDate)) {
            throw new IllegalArgumentException("Registration period start must not be after end");
        }
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isOpen(LocalDate today) {
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    @Override
    public String toString() {
        return "%s .. %s".formatted(startDate, endDate);
    }
}
