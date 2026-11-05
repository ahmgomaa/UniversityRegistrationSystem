package com.university.registration.port;

import com.university.registration.dto.Notification;
import com.university.registration.model.User;

import java.util.List;

/** Persistence port for delivered notifications. */
public interface NotificationRepository {

    void save(Notification notification);

    List<Notification> findByRecipient(User user);
}
