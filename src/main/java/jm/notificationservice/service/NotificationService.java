package jm.notificationservice.service;

import jakarta.transaction.Transactional;
import jm.notificationservice.dto.NotificationDTO;
import jm.notificationservice.model.Notification;
import org.quartz.SchedulerException;

import java.util.UUID;

public interface NotificationService {
    UUID scheduleNotification(NotificationDTO notificationDTO);

    Notification getNotification(UUID notificationId);

    boolean forceSendNotification(UUID notificationId) throws SchedulerException;

    void deleteNotification(UUID notificationId) throws SchedulerException;

    void updateNotification(Notification notification);
}
