package model;

import model.enums.StaffRole;

import java.util.ArrayList;

public class AcademicStaff extends User {

    private final Department department;
    private final ArrayList<StaffRole> roles;

    public AcademicStaff(String userId, String name, String phoneNumber, String password,
                         Department department, ArrayList<StaffRole> roles) {
        super(userId, name, phoneNumber, password);
        if (!userId.startsWith("AS-")) {
            throw new IllegalArgumentException("Staff id must look like AS-11");
        }
        if (department == null) {
            throw new IllegalArgumentException("Department must not be null");
        }
        this.department = department;
        this.roles = new ArrayList<StaffRole>();
        if (roles != null) {
            for (StaffRole role : roles) {
                this.roles.add(role);
            }
        }
    }

    public AcademicStaff(String userId, String name, String phoneNumber, User.HashedPassword passwordHash,
                         Department department, ArrayList<StaffRole> roles) {
        super(userId, name, phoneNumber, passwordHash);
        if (!userId.startsWith("AS-")) {
            throw new IllegalArgumentException("Staff id must look like AS-11");
        }
        if (department == null) {
            throw new IllegalArgumentException("Department must not be null");
        }
        this.department = department;
        this.roles = new ArrayList<StaffRole>();
        if (roles != null) {
            for (StaffRole role : roles) {
                this.roles.add(role);
            }
        }
    }

    public Department getDepartment() {
        return department;
    }

    public ArrayList<StaffRole> getRoles() {
        return roles;
    }

    public boolean hasRole(StaffRole role) {
        return roles.contains(role);
    }

    @Override
    public String toString() {
        return getName() + " [Staff, roles = " + roles + "]";
    }
}
