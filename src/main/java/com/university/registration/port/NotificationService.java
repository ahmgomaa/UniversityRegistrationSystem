package com.university.registration.port;

import com.university.registration.model.User;

/** Outbound notification port (diagram: «interface» NotificationService). */
public interface NotificationService {

    void notify(User user, String message);
}
