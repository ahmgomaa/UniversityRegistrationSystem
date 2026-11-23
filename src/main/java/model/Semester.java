package model;

import model.enums.Term;

import java.time.LocalDate;

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
        if (semesterId == null || semesterId.trim().isEmpty()) {
            throw new IllegalArgumentException("Semester id must not be empty");
        }
        if (term == null || startDate == null || endDate == null
                || withdrawalDeadline == null || registrationPeriod == null) {
            throw new IllegalArgumentException("All semester data is required");
        }
        this.semesterId = semesterId.trim();
        this.term = term;
        this.year = year;
        this.startDate = startDate;
        this.endDate = endDate;
        this.withdrawalDeadline = withdrawalDeadline;
        this.registrationPeriod = registrationPeriod;
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
        return !date.isBefore(startDate) && !date.isAfter(withdrawalDeadline);
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    @Override
    public int compareTo(Semester other) {
        if (this.year != other.year) {
            return this.year - other.year;
        }
        return this.term.ordinal() - other.term.ordinal();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Semester) {
            Semester other = (Semester) obj;
            return this.semesterId.equals(other.semesterId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return semesterId.hashCode();
    }

    @Override
    public String toString() {
        return term + "-" + year;
    }
}
