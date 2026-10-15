package com.university.registration.model;

import java.time.LocalDate;

/**
 * Represents an instructor who teaches course sections.
 */
public class Instructor extends User {

    private Department department;

    public Instructor(String userId, String name, String email, LocalDate dateOfBirth,
                      String phoneNumber, Department department) {
        super(userId, name, email, dateOfBirth, phoneNumber);
        this.department = department;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "Instructor{userId='" + getUserId() + "', name='" + getName()
                + "', department=" + department + "}";
    }
}
