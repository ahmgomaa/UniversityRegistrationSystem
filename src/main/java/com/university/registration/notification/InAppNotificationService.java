package com.university.registration.notification;

import com.university.registration.dto.Notification;
import com.university.registration.model.User;
import com.university.registration.port.NotificationRepository;
import com.university.registration.port.NotificationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Delivers notifications into per-user in-app inboxes. Writes through to a
 * {@link NotificationRepository} when one is configured so notifications
 * survive restarts; the repository is then the source of truth for reads.
 */
public class InAppNotificationService implements NotificationService {

    private final Map<String, List<Notification>> inbox = new HashMap<>();
    private final NotificationRepository repository;

    public InAppNotificationService() {
        this(null);
    }

    public InAppNotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void notify(User user, String message) {
        Notification notification = new Notification(user, message, LocalDateTime.now().withNano(0));
        inbox.computeIfAbsent(user.getUserId(), id -> new ArrayList<>()).add(notification);
        if (repository != null) {
            repository.save(notification);
        }
    }

    public List<Notification> getInbox(User user) {
        if (repository != null) {
            return repository.findByRecipient(user);
        }
        return List.copyOf(inbox.getOrDefault(user.getUserId(), List.of()));
    }
}
