package com.university.registration.model;

import com.university.registration.model.enums.StaffRole;

import java.util.Collections;
import java.util.Set;

/** Non-teaching academic staff (advisors, registrars) with one or more roles. */
public class AcademicStaff extends User {

    private final Department department;
    private final Set<StaffRole> roles;

    public AcademicStaff(String userId, String name, String email, String passwordHash, String phoneNumber,
                         Department department, Set<StaffRole> roles) {
        super(userId, name, email, passwordHash, phoneNumber);
        this.department = Objects.requireNonNull(department, "department");
        this.roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    public Department getDepartment() {
        return department;
    }

    public Set<StaffRole> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public boolean hasRole(StaffRole role) {
        return roles.contains(role);
    }

    @Override
    public String toString() {
        return "%s [Staff, %s]".formatted(getName(), roles);
    }
}
