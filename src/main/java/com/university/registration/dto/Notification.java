package com.university.registration.dto;

/**
 * Represents an in-app notification message.
 */
public class Notification {

    private final String message;

    public Notification(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}
