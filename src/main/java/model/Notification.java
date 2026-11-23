package model;

import java.time.LocalDateTime;

public class Notification {

    private final User recipient;
    private final String message;
    private final LocalDateTime sentAt;

    public Notification(User recipient, String message, LocalDateTime sentAt) {
        if (recipient == null || message == null || sentAt == null) {
            throw new IllegalArgumentException("Recipient, message and date are required");
        }
        this.recipient = recipient;
        this.message = message;
        this.sentAt = sentAt;
    }

    public User getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    @Override
    public String toString() {
        return "[" + sentAt + "] " + message;
    }
}
