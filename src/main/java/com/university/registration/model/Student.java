package com.university.registration.model;

import com.university.registration.model.enums.AcademicStatus;
import com.university.registration.model.enums.StaffRole;

import java.util.Objects;

/**
 * A registered student. Owns an {@link AcademicRecord} (composition) and
 * follows exactly one {@link Program}.
 */
public class Student extends User {

    private final String studentNumber;
    private final int admissionYear;
    private final AcademicRecord academicRecord;
    private AcademicStatus academicStatus;
    private Program program;
    private AcademicStaff advisor;

    public Student(String userId, String name, String email, String passwordHash, String phoneNumber,
                   String studentNumber, int admissionYear, AcademicStatus academicStatus,
                   Program program, AcademicStaff advisor) {
        super(userId, name, email, passwordHash, phoneNumber);
        this.studentNumber = Objects.requireNonNull(studentNumber, "studentNumber");
        this.admissionYear = admissionYear;
        this.academicStatus = Objects.requireNonNull(academicStatus, "academicStatus");
        this.program = Objects.requireNonNull(program, "program");
        this.advisor = advisor;
        this.academicRecord = new AcademicRecord();
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public int getAdmissionYear() {
        return admissionYear;
    }

    public AcademicStatus getAcademicStatus() {
        return academicStatus;
    }

    public Program getProgram() {
        return program;
    }

    public AcademicStaff getAdvisor() {
        return advisor;
    }

    public AcademicRecord getAcademicRecord() {
        return academicRecord;
    }

    /** Assigns an advisor; the staff member must hold the ADVISOR role. */
    public void assignAdvisor(AcademicStaff staff) {
        if (staff == null || !staff.hasRole(StaffRole.ADVISOR)) {
            throw new IllegalArgumentException("Advisor must be academic staff with the ADVISOR role");
        }
        this.advisor = staff;
    }

    /**
     * Administrative status change (e.g. suspension, graduation) performed by a
     * staff member holding the REGISTRAR role.
     */
    public void updateAcademicStatus(AcademicStaff registrar, AcademicStatus status) {
        if (registrar == null || !registrar.hasRole(StaffRole.REGISTRAR)) {
            throw new IllegalArgumentException("Only a REGISTRAR can change academic status");
        }
        applyStanding(status);
    }

    /** Applies a standing decision without authorization checks (used by the grading flow). */
    public void applyStanding(AcademicStatus status) {
        this.academicStatus = Objects.requireNonNull(status, "status");
    }

    /** Current level (year) within the program, derived from earned credits. */
    public int getLevel() {
        return program.levelFor(academicRecord.getEarnedCredits());
    }

    @Override
    public String toString() {
        return "%s [%s, %s]".formatted(getName(), studentNumber, academicStatus);
    }
}
