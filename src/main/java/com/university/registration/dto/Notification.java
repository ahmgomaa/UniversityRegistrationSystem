package com.university.registration.dto;

import com.university.registration.model.User;

import java.time.LocalDateTime;

/** An in-app notification delivered to a user. */
public record Notification(User recipient, String message, LocalDateTime sentAt) {
}
