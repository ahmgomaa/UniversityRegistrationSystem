package model;

import model.enums.AcademicStatus;
import model.enums.StaffRole;

public class Student extends User {

    private final String studentNumber;
    private final int admissionYear;
    private final AcademicRecord academicRecord;
    private final Program program;
    private AcademicStatus academicStatus;
    private AcademicStaff advisor;

    public Student(String userId, String name, String phoneNumber, String password,
                   String studentNumber, int admissionYear, AcademicStatus academicStatus,
                   Program program, AcademicStaff advisor) {
        super(userId, name, phoneNumber, password);
        checkStudentId(userId);
        this.studentNumber = studentNumber;
        this.admissionYear = admissionYear;
        this.academicStatus = academicStatus;
        this.program = program;
        this.advisor = checkAdvisor(advisor);
        this.academicRecord = new AcademicRecord();
    }

    public Student(String userId, String name, String phoneNumber, User.HashedPassword passwordHash,
                   String studentNumber, int admissionYear, AcademicStatus academicStatus,
                   Program program, AcademicStaff advisor) {
        super(userId, name, phoneNumber, passwordHash);
        checkStudentId(userId);
        this.studentNumber = studentNumber;
        this.admissionYear = admissionYear;
        this.academicStatus = academicStatus;
        this.program = program;
        this.advisor = checkAdvisor(advisor);
        this.academicRecord = new AcademicRecord();
    }

    private static void checkStudentId(String id) {
        if (!id.startsWith("CS-")) {
            throw new IllegalArgumentException("Student id must look like CS-1234");
        }
    }

    private static AcademicStaff checkAdvisor(AcademicStaff staff) {
        if (staff != null && !staff.hasRole(StaffRole.ADVISOR)) {
            throw new IllegalArgumentException("Advisor must have the ADVISOR role");
        }
        return staff;
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

    public void assignAdvisor(AcademicStaff staff) {
        if (staff == null) {
            throw new IllegalArgumentException("Advisor must not be null");
        }
        this.advisor = checkAdvisor(staff);
    }

    public void updateAcademicStatus(AcademicStaff registrar, AcademicStatus status) {
        if (registrar == null || !registrar.hasRole(StaffRole.REGISTRAR)) {
            throw new IllegalArgumentException("Only a REGISTRAR can change academic status");
        }
        applyStanding(status);
    }

    public void applyStanding(AcademicStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null");
        }
        this.academicStatus = status;
    }

    public int getLevel() {
        return program.levelFor(academicRecord.getEarnedCredits());
    }

    @Override
    public String toString() {
        return getName() + " [" + studentNumber + ", " + academicStatus + "]";
    }
}
