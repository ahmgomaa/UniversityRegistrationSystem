package com.university.registration.model;

import com.university.registration.model.enums.StaffRole;

import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an academic staff member (e.g., advisor, registrar).
 */
public class AcademicStaff extends User {

    private Department department;
    private final Set<StaffRole> roles;

    public AcademicStaff(String userId, String name, String email, LocalDate dateOfBirth,
                         String phoneNumber, Department department, Set<StaffRole> roles) {
        super(userId, name, email, dateOfBirth, phoneNumber);
        this.department = department;
        this.roles = (roles != null) ? EnumSet.copyOf(roles) : EnumSet.noneOf(StaffRole.class);
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Set<StaffRole> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    /**
     * Checks if this staff member has the specified role.
     *
     * @param role the role to check
     * @return true if the staff member has this role
     */
    public boolean hasRole(StaffRole role) {
        Objects.requireNonNull(role, "role must not be null");
        return roles.contains(role);
    }

    @Override
    public String toString() {
        return "AcademicStaff{userId='" + getUserId() + "', name='" + getName()
                + "', roles=" + roles + "}";
    }
}
