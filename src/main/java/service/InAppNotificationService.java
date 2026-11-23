package service;

import model.Notification;
import model.User;
import port.NotificationService;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class InAppNotificationService implements NotificationService {

    private final ArrayList<Notification> inbox;

    public InAppNotificationService(ArrayList<Notification> inbox) {
        this.inbox = inbox;
    }

    @Override
    public void notify(User user, String message) {
        inbox.add(new Notification(user, message, LocalDateTime.now().withNano(0)));
    }

    public ArrayList<Notification> getInbox(User user) {
        ArrayList<Notification> result = new ArrayList<Notification>();
        for (Notification n : inbox) {
            if (n.getRecipient().equals(user)) {
                result.add(n);
            }
        }
        return result;
    }
}
