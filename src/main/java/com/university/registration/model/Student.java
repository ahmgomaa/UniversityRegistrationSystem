package com.university.registration.model;

import java.time.LocalDate;

import com.university.registration.model.enums.AcademicStatus;
import com.university.registration.model.enums.StaffRole;

import java.util.Objects;

/**
 * Represents a student enrolled in the university.
 * Tracks academic record, program, standing, and advisor.
 */
public class Student extends User {

    private final String studentNumber;
    private final int admissionYear;
    private Program program;
    private AcademicRecord academicRecord;
    private AcademicStatus academicStatus;
    private AcademicStaff advisor;

    public Student(String userId, String name, String email, LocalDate dateOfBirth,
                   String phoneNumber, String studentNumber, int admissionYear, Program program) {
        super(userId, name, email, dateOfBirth, phoneNumber);
        this.studentNumber = Objects.requireNonNull(studentNumber, "studentNumber must not be null");
        this.admissionYear = admissionYear;
        this.program = Objects.requireNonNull(program, "program must not be null");
        this.academicRecord = new AcademicRecord();
        this.academicStatus = AcademicStatus.ACTIVE;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public int getAdmissionYear() {
        return admissionYear;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = Objects.requireNonNull(program, "program must not be null");
    }

    public AcademicRecord getAcademicRecord() {
        return academicRecord;
    }

    public AcademicStatus getAcademicStatus() {
        return academicStatus;
    }

    /**
     * Assigns an academic staff member as the student's advisor.
     * The staff member must have the ADVISOR role.
     *
     * @param staff the academic staff to assign as advisor
     * @throws IllegalArgumentException if the staff member does not have the ADVISOR role
     */
    public void assignAdvisor(AcademicStaff staff) {
        Objects.requireNonNull(staff, "staff must not be null");
        if (!staff.hasRole(StaffRole.ADVISOR)) {
            throw new IllegalArgumentException("Staff member must have ADVISOR role");
        }
        this.advisor = staff;
    }

    public AcademicStaff getAdvisor() {
        return advisor;
    }

    /**
     * Updates the student's academic standing.
     * Only a registrar is authorized to perform this action.
     *
     * @param registrar the academic staff making the change (must have REGISTRAR role)
     * @param status    the new academic status
     * @throws IllegalArgumentException if the registrar does not have the REGISTRAR role
     */
    public void updateAcademicStanding(AcademicStaff registrar, AcademicStatus status) {
        Objects.requireNonNull(registrar, "registrar must not be null");
        Objects.requireNonNull(status, "status must not be null");
        if (!registrar.hasRole(StaffRole.REGISTRAR)) {
            throw new IllegalArgumentException("Only registrars can update academic standing");
        }
        this.academicStatus = status;
    }

    /**
     * Applies standing status based on the student's current CGPA and program thresholds.
     * If CGPA falls below the program's probation threshold, status becomes PROBATION;
     * otherwise, it reverts to ACTIVE.
     */
    public void applyStandingStatus() {
        double cgpa = academicRecord.calculateCGPA();
        if (program.getProbationGPA() > 0 && cgpa < program.getProbationGPA()) {
            this.academicStatus = AcademicStatus.PROBATION;
        } else {
            this.academicStatus = AcademicStatus.ACTIVE;
        }
    }

    @Override
    public String toString() {
        return "Student{studentNumber='" + studentNumber + "', name='" + getName() + "'}";
    }
}
