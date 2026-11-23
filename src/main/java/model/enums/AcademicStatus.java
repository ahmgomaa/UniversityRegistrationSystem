package model.enums;

public enum AcademicStatus {
    ACTIVE,
    PROBATION,
    SUSPENDED,
    GRADUATED;

    public boolean canRegister() {
        return this == ACTIVE || this == PROBATION;
    }
}
