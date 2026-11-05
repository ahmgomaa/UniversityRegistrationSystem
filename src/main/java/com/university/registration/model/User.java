package com.university.registration.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Base class for every person interacting with the system
 * (Student, Instructor, AcademicStaff).
 */
public abstract class User {

    private String userId;
    private String name;
    private String email;
    private String passwordHash;
    private String phoneNumber;

    protected User(String userId, String name, String email, String passwordHash, String phoneNumber) {
        this.userId = Objects.requireNonNull(userId, "userId");
        this.name = Objects.requireNonNull(name, "name");
        this.email = Objects.requireNonNull(email, "email");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
        this.phoneNumber = phoneNumber;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public void setEmail(String email) {
        this.email = Objects.requireNonNull(email, "email");
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /** SHA-256 hex digest of a raw password. */
    public static String hash(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16))
                   .append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public boolean verifyPassword(String rawPassword) {
        return MessageDigest.isEqual(
                passwordHash.getBytes(StandardCharsets.UTF_8),
                hash(rawPassword).getBytes(StandardCharsets.UTF_8));
    }

    public void changePassword(String oldPassword, String newPassword) {
        if (!verifyPassword(oldPassword)) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }
        this.passwordHash = hash(newPassword);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return userId.equals(user.userId);
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }

    @Override
    public String toString() {
        return "%s (%s)".formatted(name, userId);
    }
}
