package com.university.registration.model;

import java.util.Objects;

/** Teaching staff member; teaches {@link CourseSection}s in a department. */
public class Instructor extends User {

    private final Department department;

    public Instructor(String userId, String name, String email, String passwordHash, String phoneNumber,
                      Department department) {
        super(userId, name, email, passwordHash, phoneNumber);
        this.department = Objects.requireNonNull(department, "department");
    }

    public Department getDepartment() {
        return department;
    }

    @Override
    public String toString() {
        return "%s [Instructor, %s]".formatted(getName(), department.getCode());
    }
}
