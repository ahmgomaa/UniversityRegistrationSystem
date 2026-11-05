package com.university.registration.model;

import com.university.registration.model.enums.Term;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A semester (term + year). Owns its {@link RegistrationPeriod} (composition)
 * and carries the withdrawal deadline used by the withdraw flow.
 */
public class Semester implements Comparable<Semester> {

    private final String semesterId;
    private final Term term;
    private final int year;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalDate withdrawalDeadline;
    private final RegistrationPeriod registrationPeriod;

    public Semester(String semesterId, Term term, int year,
                    LocalDate startDate, LocalDate endDate, LocalDate withdrawalDeadline,
                    RegistrationPeriod registrationPeriod) {
        this.semesterId = Objects.requireNonNull(semesterId, "semesterId");
        this.term = Objects.requireNonNull(term, "term");
        this.year = year;
        this.startDate = Objects.requireNonNull(startDate, "startDate");
        this.endDate = Objects.requireNonNull(endDate, "endDate");
        this.withdrawalDeadline = Objects.requireNonNull(withdrawalDeadline, "withdrawalDeadline");
        this.registrationPeriod = Objects.requireNonNull(registrationPeriod, "registrationPeriod");
    }

    public String getSemesterId() {
        return semesterId;
    }

    public Term getTerm() {
        return term;
    }

    public int getYear() {
        return year;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDate getWithdrawalDeadline() {
        return withdrawalDeadline;
    }

    public RegistrationPeriod getRegistrationPeriod() {
        return registrationPeriod;
    }

    public boolean isWithdrawalAllowed(LocalDate date) {
        return !date.isAfter(withdrawalDeadline) && !date.isBefore(startDate);
    }

    /** Whether this semester's date range contains the given date. */
    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /** Whether this semester's date range overlaps another semester's range. */
    public boolean contains(Semester other) {
        return !other.endDate.isBefore(startDate) && !other.startDate.isAfter(endDate);
    }

    /** Chronological order: by year, then SPRING &lt; SUMMER &lt; FALL. */
    @Override
    public int compareTo(Semester other) {
        int byYear = Integer.compare(year, other.year);
        return byYear != 0 ? byYear : Integer.compare(term.ordinal(), other.term.ordinal());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Semester semester)) return false;
        return semesterId.equals(semester.semesterId);
    }

    @Override
    public int hashCode() {
        return semesterId.hashCode();
    }

    @Override
    public String toString() {
        return "%s-%d".formatted(term, year);
    }
}
