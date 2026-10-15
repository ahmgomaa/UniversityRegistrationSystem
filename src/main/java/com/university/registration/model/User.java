package com.university.registration.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Base class representing a user in the university system.
 * Extended by Student, Instructor, and AcademicStaff.
 */
public abstract class User {

    private final String userId;
    private String name;
    private String email;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String passwordHash;

    protected User(String userId, String name, String email, LocalDate dateOfBirth, String phoneNumber) {
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name must not be null");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Objects.requireNonNull(email, "email must not be null");
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Verifies if the given plain-text password matches the stored hash.
     *
     * @param password the plain-text password to verify
     * @return true if the password matches
     */
    public boolean verifyPassword(String password) {
        if (password == null || passwordHash == null) {
            return false;
        }
        // Simplified: in production, use BCrypt or similar
        return passwordHash.equals(Integer.toString(password.hashCode()));
    }

    /**
     * Changes the user's password after verifying the old password.
     *
     * @param oldPassword the current password
     * @param newPassword the new password to set
     * @return true if the password was changed successfully
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        if (passwordHash == null || verifyPassword(oldPassword)) {
            this.passwordHash = Integer.toString(newPassword.hashCode());
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{userId='" + userId + "', name='" + name + "'}";
    }
}
