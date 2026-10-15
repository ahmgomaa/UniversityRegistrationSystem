package com.university.registration.service;

import com.university.registration.model.User;

/**
 * Interface for sending notifications to users.
 */
public interface NotificationService {

    /**
     * Sends a notification message to the specified user.
     *
     * @param user    the user to notify
     * @param message the notification message
     */
    void notify(User user, String message);
}
