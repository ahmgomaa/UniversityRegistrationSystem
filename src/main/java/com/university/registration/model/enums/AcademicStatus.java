package com.university.registration.model.enums;

/**
 * Represents the academic standing of a student.
 */
public enum AcademicStatus {
    ACTIVE,
    PROBATION,
    SUSPENDED,
    DISMISSED;

    /**
     * Determines whether a student with this academic status is allowed to register for courses.
     *
     * @return true if the student can register (ACTIVE or PROBATION), false otherwise
     */
    public boolean canRegister() {
        return this == ACTIVE || this == PROBATION;
    }
}
