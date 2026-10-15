package com.university.registration.model;

import com.university.registration.model.enums.Term;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents an academic semester (e.g., Fall 2025, Spring 2026).
 * Contains registration and withdrawal deadline information.
 */
public class Semester {

    private final Term term;
    private final int year;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final RegistrationPeriod registrationPeriod;
    private final LocalDate addWithdrawDeadline;
    private final LocalDate courseWithdrawDeadline;

    public Semester(Term term, int year, LocalDate startDate, LocalDate endDate,
                    RegistrationPeriod registrationPeriod,
                    LocalDate addWithdrawDeadline, LocalDate courseWithdrawDeadline) {
        this.term = Objects.requireNonNull(term, "term must not be null");
        this.year = year;
        this.startDate = Objects.requireNonNull(startDate, "startDate must not be null");
        this.endDate = Objects.requireNonNull(endDate, "endDate must not be null");
        this.registrationPeriod = Objects.requireNonNull(registrationPeriod,
                "registrationPeriod must not be null");
        this.addWithdrawDeadline = addWithdrawDeadline;
        this.courseWithdrawDeadline = courseWithdrawDeadline;
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

    public RegistrationPeriod getRegistrationPeriod() {
        return registrationPeriod;
    }

    public LocalDate getAddWithdrawDeadline() {
        return addWithdrawDeadline;
    }

    public LocalDate getCourseWithdrawDeadline() {
        return courseWithdrawDeadline;
    }

    /**
     * Checks if registration is currently open for this semester.
     *
     * @param today the current date
     * @return true if today falls within the registration period
     */
    public boolean isInRegistrationPeriod(LocalDate today) {
        return registrationPeriod.isOpen(today);
    }

    /**
     * Compares this semester to another by year and term ordering (SPRING < SUMMER < FALL).
     *
     * @param other the semester to compare to
     * @return a negative integer, zero, or a positive integer
     */
    public int compareToThatTerm(Semester other) {
        int yearCompare = Integer.compare(this.year, other.year);
        if (yearCompare != 0) {
            return yearCompare;
        }
        return Integer.compare(this.term.ordinal(), other.term.ordinal());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Semester semester)) return false;
        return year == semester.year && term == semester.term;
    }

    @Override
    public int hashCode() {
        return Objects.hash(term, year);
    }

    @Override
    public String toString() {
        return term + " " + year;
    }
}
