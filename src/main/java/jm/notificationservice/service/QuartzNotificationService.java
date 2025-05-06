package jm.notificationservice.service;

import jm.notificationservice.model.Notification;
import org.quartz.SchedulerException;

import java.util.Date;
import java.util.UUID;

public interface QuartzNotificationService {
    void scheduleNotification(Notification notification, long retryDelayMillis) throws SchedulerException;

    void cancelNotification(UUID notificationId) throws SchedulerException;

    void scheduleDelete(Notification notification);

    Date adjustToAllowedHours(Notification notification);
}
