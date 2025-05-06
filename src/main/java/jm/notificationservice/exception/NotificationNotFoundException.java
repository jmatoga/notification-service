package jm.notificationservice.exception;

import java.util.UUID;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(UUID id) {
        super("Notification with ID: " + id + " not found.");
    }
}
