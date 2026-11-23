package model;

import java.time.LocalDate;

public class RegistrationPeriod {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public RegistrationPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Registration dates must not be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Registration start must not be after end");
        }
        this.startDate = startDate;
        this.endDate = endDate;
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
        return "Registration: " + startDate + " to " + endDate;
    }
}
