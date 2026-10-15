package com.university.registration.service;

import com.university.registration.dto.Notification;
import com.university.registration.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-app notification service implementation.
 * Stores notifications in memory for retrieval.
 */
public class AppNotificationService implements NotificationService {

    private final List<Notification> notifications = new ArrayList<>();

    @Override
    public void notify(User user, String message) {
        Notification notification = new Notification(
                "[To: " + user.getName() + "] " + message);
        notifications.add(notification);
    }

    /**
     * Returns all notifications that have been sent.
     *
     * @return unmodifiable list of notifications
     */
    public List<Notification> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }

    /**
     * Clears all stored notifications.
     */
    public void clearNotifications() {
        notifications.clear();
    }
}
