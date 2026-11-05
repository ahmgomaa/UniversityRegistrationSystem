package com.university.registration.model.enums;

/**
 * Academic standing of a student. Controls whether the student may register at
 * all, and which credit limit applies (see Program).
 */
public enum AcademicStatus {
    ACTIVE,
    PROBATION,
    SUSPENDED,
    GRADUATED;

    /** Only students in good or probationary standing may register. */
    public boolean canRegister() {
        return this == ACTIVE || this == PROBATION;
    }
}
